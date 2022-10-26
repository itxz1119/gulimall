package com.atguigu.gulimall.product.exception;

import lombok.Data;

@Data
public class GulimallException extends RuntimeException{
    private Integer code;
    private String message;

}
