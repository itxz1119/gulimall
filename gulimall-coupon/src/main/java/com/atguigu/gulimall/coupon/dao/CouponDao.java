package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 11:55:09
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
