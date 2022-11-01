package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.MemberPrice;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReductionTo(SkuReductionTo skuReductionTo) {
        //保存sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReductionTo, skuLadderEntity);
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuLadderEntity.getFullCount() > 0){
            skuLadderService.save(skuLadderEntity);
        }

        //保存sms_sku_full_reduction
        SkuFullReductionEntity skuFullReduction = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReduction);
        skuFullReduction.setAddOther(skuReductionTo.getPriceStatus());
        if (skuFullReduction.getFullPrice().compareTo(new BigDecimal("0")) == 1){
            this.save(skuFullReduction);
        }

        //保存sms_member_price
        List<MemberPrice> memberPriceList = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPriceList.stream().map(item -> {
            MemberPriceEntity memberPrice = new MemberPriceEntity();
            memberPrice.setSkuId(skuReductionTo.getSkuId());
            memberPrice.setMemberLevelId(item.getId());
            memberPrice.setMemberLevelName(item.getName());
            memberPrice.setMemberPrice(item.getPrice());
            memberPrice.setAddOther(1);
            return memberPrice;
        }).filter(item ->{
            return item.getMemberPrice().compareTo(new BigDecimal("0")) == 1;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}
