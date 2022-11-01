package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 远程调用优惠卷服务
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 执行过程：
     * 1.调用这个方法之后 扫描到@RequestBody注解，将实体类转为json
     * 2.找到gulimall-coupon服务，给/coupon/spubounds/save发送请求，将上一步的json放在请求体位置
     * 3.找到接口之后 将json转为对象；
     * 只要json模型是兼容的 方法参数可以不是同一个实体类
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    public R saveSpuBound(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveskuReduction(SkuReductionTo skuReductionTo);
}
