package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.atguigu.gulimall.product.vo.Catelog3Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /*
     * 获取三级分类
     * */
    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //组成树形结构
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    menu.setChildren(getChildren(menu, entities));
                    return menu;
                }).sorted((m1, m2) -> {
                    return (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort());
                }).collect(Collectors.toList());
        return level1Menus;
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> entities) {
        List<CategoryEntity> children = entities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((menu) -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((m1, m2) -> {
            return (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

    /*
     *
     * 删除菜单
     * */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查要删除的菜单是否在其他地方被引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findParent(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        //需要把集合顺序转换
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /*
     * 更新冗余字段
     * */
    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        baseMapper.updateById(category);
        if (StringUtils.hasLength(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    /**
     * 1.默认行为
     * @Cacheable({"category"})
     * 1）代表当前方法的结果需要缓存，如果缓存中有，方法不用调用；
     * 如果缓存中没有，调用方法，最后将方法的结果放入缓存中。
     * 2）放在redis缓存中的key是自动生成的--》category::SimpleKey []。缓存的value值是序列化之后的；
     * 3）默认过期时间--》-1
     *
     * 2.自定义
     * 1）指定生成的key
     *  a: @Cacheable(value = {"category"}, key = "'level1Categorys'")-->key为category::level1Categorys
     *  b: @Cacheable(value = {"category"}, key = "#root.method.name")-->key为category::方法名
     * 2）指定缓存的数据存活时间-->在配置文件中指定 redis.time-to-live
     * 3）将数据存为json格式
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.method.name") //每一个需要缓存的数据，都需要指定放在哪个名字的缓存（缓存分区-》按照业务类型），是个数组，可以写多个
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("方法被调========");
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("cat_level", 1);
        return this.list(wrapper);
    }

    /**
     * 1.解决缓存穿透：空结果缓存
     * 2.缓存雪崩：设置过期时间
     * 3.缓存击穿：加锁
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        //1.判断缓存中是否有数据
        String catalogJson = ops.get("catalogJson");
        //2.没有的话  -->进行查询
        if (StringUtils.isEmpty(catalogJson)) {
            System.out.println("缓存不命中======查询数据库");
            Map<String, List<Catelog2Vo>> jsonFromDb = getCatalogJsonFromDbWithRedisLock();
            return jsonFromDb;
        }
        System.out.println("缓存命中======");
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    /**
     * 使用redisson加锁
     * 缓存数据一致性问题：双写模式--》改完数据库，改缓存；失效模式--》改完数据库，删除缓存。
     * 两个模式都可能出现脏数据。
     * 一致性问题解决--》1.所有的缓存都有过期时间，数据过期触发自动更新。2.加分布式读写锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        //加锁成功  执行业务
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    /**
     * 分布式锁
     * 从数据库查询并封装数据
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        //占分布式锁， 去redis占坑  设置过期时间，避免占锁之后出现异常，出现死锁
        String uuid = UUID.randomUUID().toString();
        //加锁和设置过期时间必须是同步的，原子操作
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            //加锁成功  执行业务
            Map<String, List<Catelog2Vo>> dataFromDb;
            /*
             *  删除问题：如果业务用时过长，设置的锁已经过期，可能把别人正在持有的的锁删除掉
             *  解决：使用uuid，每个人的锁的值都不一样，删除之前进行值对比
             *  但是还有问题--》 假如key过期时间为10s，一个业务用了9.5s，在向redis发送get请求获取值用了0.3秒，
             * 这时，值已经获取到了，并且是自己的值，redis返回的过程中，用了0.5秒，当0.2s的时候，lock过期了，
             * 别的进程获取了新锁，也叫lock。这时，会把别人的锁删除掉。
             * 所以删除锁时，需要保证查询锁的值和删除是同步进行的，原子操作，需要使用lua脚本解锁
             */
            /*String lock1 = stringRedisTemplate.opsForValue().get("lock");
            if (uuid.equals(lock1)) {
                //删除自己的锁
                stringRedisTemplate.delete("lock");
            }*/
            // KEYS[1] ==> lock   ARGV[1] ==> uuid
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            //加锁失败  重试 -->自旋的方式
            //休眠100ms,再重试
            //Thread.sleep(100);
            return getCatalogJsonFromDbWithRedisLock(); //重试 -->自旋的方式
        }

    }

    /**
     * 本地锁
     * 从数据库查询并封装数据
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        // todo 本地锁：只能锁住当前进程
        //保证原子性：将查询数据库和将数据放入到redis中，两个操作要在同一把锁下进行，否则可能查询多次数据库
        synchronized (this) {
            return getDataFromDb();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.hasLength(catalogJson)) {
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        System.out.println("查询了数据库======");
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
        List<CategoryEntity> level1List = getParent_cid(categoryEntityList, 0L);
        Map<String, List<Catelog2Vo>> map = level1List.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<Catelog2Vo> catelog2VoList = null;
            //根据一级分类的cat_id 查出二级分类
            List<CategoryEntity> level2List = getParent_cid(categoryEntityList, v.getCatId());
            if (!level2List.isEmpty()) {
                catelog2VoList = level2List.stream().map(level2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    //根据2级分类的cat_id 查出3级分类
                    List<CategoryEntity> catelog3VoList = getParent_cid(categoryEntityList, level2.getCatId());
                    if (!catelog3VoList.isEmpty()) {
                        List<Catelog3Vo> vo3List = catelog3VoList.stream().map(level3 -> {
                            Catelog3Vo catelog3Vo = new Catelog3Vo(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(vo3List);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2VoList;
        }));
        // 修改之前的逻辑， 查完数据库就将数据放在redis中
        String s = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catalogJson", s);
        return map;
    }


    /**
     * 查出某分类下所有的子分类
     *
     * @param list
     * @param parentCid
     * @return
     */
    private List<CategoryEntity> getParent_cid(List<CategoryEntity> list, Long parentCid) {
        List<CategoryEntity> collect = list.stream().filter(item -> parentCid == item.getParentCid()).collect(Collectors.toList());
        return collect;
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }
}
