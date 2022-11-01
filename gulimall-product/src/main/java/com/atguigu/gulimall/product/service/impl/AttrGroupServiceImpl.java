package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasLength(key)){
            wrapper.and((item)->{
                item.eq("attr_group_id",key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0){
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = baseMapper.selectPage(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttr(Long catelogId) {
        //查出 分类下所有 AttrGroup（属性分组）
        List<AttrGroupEntity> attrGroupList = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //查出每个属性分组下的规格参数
        List<AttrGroupWithAttrVo> collect = attrGroupList.stream().map((attrGroup) -> {
            AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(attrGroup, attrGroupWithAttrVo);
            //根据AttrGroupId 获取 AttrEntity
            List<AttrEntity> relationList = attrService.getRelation(attrGroup.getAttrGroupId());
            //给AttrGroupWithAttrVo设置 attrList
            attrGroupWithAttrVo.setAttrs(relationList);
            return attrGroupWithAttrVo;
        }).collect(Collectors.toList());
        return collect;
    }

}
