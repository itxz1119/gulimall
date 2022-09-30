package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性
 * 
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 09:16:14
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}
