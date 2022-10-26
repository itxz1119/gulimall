package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GlobalException {

    /*
    * 数据校验异常
    * */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleVaildExcetion(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题{},异常类型{}", e.getMessage(), e.getClass());
        BindingResult result = e.getBindingResult();
        HashMap<String, String> map = new HashMap<>();
        result.getFieldErrors().forEach((item) -> {
            map.put(item.getField(), item.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(),BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", map);
    }

    /*
    * 全局异常处理
    * */
    @ExceptionHandler(Exception.class)
    public R error(Exception e){
        log.error("系统出现问题{},异常类型{}", e.getMessage(), e.getClass());
        e.printStackTrace();
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMessage());
    }

    /*
    * 特定异常处理
    * */
    @ExceptionHandler(GulimallException.class)
    public R error(GulimallException e){
        log.error("系统出现问题{},异常类型{}", e.getMessage(), e.getClass());
        e.printStackTrace();
        return R.error(e.getCode(), e.getMessage());
    }
}
