package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 品牌
 *
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 09:45:09
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);
        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    // 实体类属性没有指定分组，该属性的注解就不会生效
    public R save(@RequestBody @Validated({AddGroup.class}) BrandEntity brand /* BindingResult result*/) {
        /*if (result.hasErrors()) {
            HashMap<String, String> map = new HashMap<>();
            result.getFieldErrors().forEach((item) -> {
                //获得错误提示
                String message = item.getDefaultMessage();
                //获取错误的属性名字
                String field = item.getField();
                map.put(field, message);
            });
            return R.error(500,"数据不合法").put("data", map);
        }*/
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@RequestBody @Validated(UpdateGroup.class) BrandEntity brand) {
        brandService.updateDetail(brand);
        return R.ok();
    }
    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@RequestBody @Validated(UpdateStatusGroup.class) BrandEntity brand) {
        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

    @RequestMapping("/hello")
    //@RequiresPermissions("product:brand:delete")
    public String hello() {
        return "hello";
    }


}
