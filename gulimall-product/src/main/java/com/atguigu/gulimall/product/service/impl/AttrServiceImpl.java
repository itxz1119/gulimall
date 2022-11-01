package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        //添加表pms_attr的基本信息
        baseMapper.insert(attrEntity);
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        //这里的attrId需要是attrEntity中的属性,执行完添加之后 该属性才会有值，
        // 前端传来的时候attrVo中的attrId是空的
        //如果是基本属性 才向分组关系表中 添加数据
        if (attrVo.getAttrType() == 1 && attrVo.getAttrGroupId() != null){
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            attrAttrgroupRelationDao.insert(relationEntity);
        }

    }

    /*
     * 查询 规格参数 和 销售属性
     *
     * */
    @Override
    public PageUtils baseList(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_type", "base".equalsIgnoreCase(attrType) ? 1 : 0);
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (StringUtils.hasLength(key)) {
            wrapper.and((item) -> {
                item.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = baseMapper.selectPage(new Query<AttrEntity>().getPage(params), wrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVoList = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //设置所属分组
            if ("base".equalsIgnoreCase(attrType)){
                AttrAttrgroupRelationEntity attrId =
                        attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrId != null){
                    if (attrId.getAttrGroupId() != null) {
                        AttrGroupEntity groupEntity =
                                attrGroupDao.selectById(attrId.getAttrGroupId());
                        attrRespVo.setGroupName(groupEntity.getAttrGroupName());
                    }
                }
            }

            //所属分类
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVoList);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = baseMapper.selectById(attrId);
        AttrRespVo respVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, respVo);
        //设置分组信息
        if (attrEntity.getAttrType() == 1){
            AttrAttrgroupRelationEntity groupRelation =
                    attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (groupRelation != null) {
                respVo.setAttrGroupId(groupRelation.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(groupRelation.getAttrGroupId());
                if (attrGroupEntity != null) {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        //设置分类信息
        Long[] catelogPath = categoryService.findParent(respVo.getCatelogId());
        respVo.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(respVo.getCatelogId());
        if (categoryEntity != null) {
            respVo.setCatelogName(categoryEntity.getName());
        }
        return respVo;
    }

    @Override
    public void updateAttrResp(AttrRespVo attrRespVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrRespVo, attrEntity);
        baseMapper.updateById(attrEntity);
        //修改分组id
        if (attrRespVo.getAttrType() == 1 && attrRespVo.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrRespVo.getAttrId());
            relationEntity.setAttrGroupId(attrRespVo.getAttrGroupId());
            attrAttrgroupRelationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrRespVo.getAttrId()));
        }

    }

    /**
     * 根据attrgroupId 获取 关联的规格参数AttrEntity
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelation(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> groupIds =
                attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrIds = groupIds.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        if (attrIds.isEmpty()){
            return null;
        }
        List<AttrEntity> attrList = baseMapper.selectBatchIds(attrIds);
        return attrList;
    }

    @Override
    public void deleteRelation(AttrAttrgroupRelationEntity[] vos) {
        List<AttrAttrgroupRelationEntity> relationList = Arrays.asList(vos);
        relationDao.deleteBatchRelation(relationList);
    }

    /**
     * 获取当前属性分组下，没有关联的规格参数
     * @param attrgroupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getNoRelation(Long attrgroupId, Map<String, Object> params) {
        //1.根据attrgroupId（属性分组的id） 查询到 catelogId（分类id）
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //2.当前分类下的  当前分组只能关联别的分组没有引用的规格参数
        //2.1 获取当前分类下的其他分组
        List<AttrGroupEntity> otherAttrGroupList = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> attrGroupIdList = otherAttrGroupList.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        if (attrGroupIdList.isEmpty()){
            return null;
        }
        //2.2 获取当前分类下 所有分组关联的规格参数
        List<AttrAttrgroupRelationEntity> groupRelationList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIdList));
        List<Long> attrIdList = groupRelationList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        //2.3 获取当前分组下， 没有被关联的规格参数
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", 1);

        if (!attrIdList.isEmpty()){
            wrapper.notIn("attr_id", attrIdList);
        }
        String key = (String) params.get("key");
        if (StringUtils.hasLength(key)){
            wrapper.and((item)->{
                item.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    /**
     * 删除规格参数：
     * 先判断该参数是否在属性分组中关联，如果关联，删除关系表中的数据
     * @param attrIds
     */
    @Override
    public void removeAndRelationByIds(List<Long> attrIds) {
        for (Long attrId : attrIds) {
            attrAttrgroupRelationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        }
        baseMapper.deleteBatchIds(attrIds);
    }

}
