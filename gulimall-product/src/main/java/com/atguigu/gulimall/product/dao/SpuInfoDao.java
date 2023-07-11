package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 *
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 09:16:14
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateStatusBySpuId(@Param("spuId") Long spuId, @Param("status") int status);
}
