package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.SkuStockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 11:37:37
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuStockVo> getSkusHasStock(List<Long> skuIds);
}

