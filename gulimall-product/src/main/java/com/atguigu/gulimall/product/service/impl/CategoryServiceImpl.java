package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
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
        /*
         *第一种方法  循环
         * */
        /*List<CategoryEntity> levelOneMenus = entities.stream().filter(categoryEntity -> categoryEntity.getCatLevel() == 1)
                .collect(Collectors.toList());

        List<CategoryEntity> levelTwoMenus = entities.stream().filter(categoryEntity -> categoryEntity.getCatLevel() == 2)
                .collect(Collectors.toList());

        List<CategoryEntity> levelThreeMenus = entities.stream().filter(categoryEntity -> categoryEntity.getCatLevel() == 3)
                .collect(Collectors.toList());

        List<CategoryEntity> finalList = new ArrayList<>();

        for (CategoryEntity level1 : levelOneMenus) {
            List<CategoryEntity> list1 = new ArrayList<>();
            for (CategoryEntity level2 : levelTwoMenus) {
                List<CategoryEntity> list2 = new ArrayList<>();
                for (CategoryEntity level3 : levelThreeMenus) {
                    if (level3.getParentCid() == level2.getCatId()) {
                        list2.add(level3);
                    }
                }
                level2.setChildren(list2);
                if (level2.getParentCid() == level1.getCatId()) {
                    list1.add(level2);
                }
            }
            level1.setChildren(list1);
            finalList.add(level1);
        }
        List<CategoryEntity> list = finalList.stream().sorted(Comparator.comparingInt(CategoryEntity::getSort)).collect(Collectors.toList());
        return list;*/

        /*
         * 第二种方法 递归
         * */
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
        if (StringUtils.hasLength(category.getName())){
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths){
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }
}
