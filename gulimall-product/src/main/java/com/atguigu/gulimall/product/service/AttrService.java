package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 09:16:14
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attrVo);

    PageUtils baseList(Map<String, Object> params, Long catelogId, String attrType);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttrResp(AttrRespVo attrRespVo);

    List<AttrEntity> getRelation(Long attrgroupId);

    void deleteRelation(AttrAttrgroupRelationEntity[] vos);

    PageUtils getNoRelation(Long attrgroupId,Map<String, Object> params);

    void removeAndRelationByIds(List<Long> asList);

    List<Long> getSearchTypeIdbyAttrId(List<Long> attrIds);
}

