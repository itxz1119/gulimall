package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {

    private Set<Integer> set = new HashSet<>();
    /*
    * 初始化方法
    * */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        //可以获得属性上注解的值 例如  属性上标注 @ListValue(vals = {0,1})
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    /*
    * 判断是否校验成功
    * Integer value 这个参数值为前端传递过来的需要校验的值
    * */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        //判断set集合是否包含这个值 不包含返回false
        return set.contains(value);
    }
}
