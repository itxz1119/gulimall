package com.atguigu.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuEsModel {

    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;
    // 销售数量
    private Long saleCount;
    //是否有库存
    private Boolean hasStock;

    private Long hotScore;

    private Long brandId;
    // 分类id
    private Long catalogId;

    private String brandName;

    private String brandImg;

    private String catalogName;

    private List<Attrs> attrs;
}

