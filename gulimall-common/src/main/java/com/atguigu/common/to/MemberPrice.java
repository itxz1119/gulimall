package com.atguigu.common.to;

import java.math.BigDecimal;
public class MemberPrice {
    private Long id;
    private String name;
    private BigDecimal price;

    private String userName;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
