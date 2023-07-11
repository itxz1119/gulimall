package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergeVo {
    private Long purchaseId; //采购单的id
    private List<Long> items;
}
