package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 11:34:33
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
