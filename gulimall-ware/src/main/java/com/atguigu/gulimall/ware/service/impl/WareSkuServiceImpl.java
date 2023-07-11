package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.SkuStockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (StringUtils.hasLength(skuId)){
            wrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (StringUtils.hasLength(wareId)){
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断当前库存中是否有该商品 没有是新增 有是修改
        WareSkuEntity wareSkuEntity = baseMapper.selectOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntity == null){
            WareSkuEntity wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setWareId(wareId);
            wareSku.setStock(skuNum);
            wareSku.setStockLocked(0);
            // 调用远程接口，查询sku的名称
            //如果发生错误，什么都不做 放在try里面 事务不会回滚
            //1.try方法
            //todo 不用try怎么完成，高级部分讲解
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0){
                    Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    String skuName = (String) skuInfo.get("skuName");
                    wareSku.setSkuName(skuName);
                }
            } catch (Exception e) {

            }
            baseMapper.insert(wareSku);
        } else {
            baseMapper.updateStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuStockVo> collect = skuIds.stream().map(skuId -> {
            Long stock = baseMapper.selectStockBySkuId(skuId);
            SkuStockVo skuStockVo = new SkuStockVo();
            skuStockVo.setSkuId(skuId);
            if (stock == null ){
                skuStockVo.setHasStock(false);
            } else {
                skuStockVo.setHasStock(stock > 0);
            }
            return skuStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

}
