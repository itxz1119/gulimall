package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品属性
 *
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 09:45:09
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService attrValueService;
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }
    /**
     * 列表
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    //@RequiresPermissions("product:attr:list")
    public R baseList(@RequestParam Map<String, Object> params,
                      @PathVariable Long catelogId,
                      @PathVariable String attrType){
        PageUtils page = attrService.baseList(params, catelogId, attrType);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrRespVo respVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", respVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attrVo){
		attrService.saveAttr(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrRespVo attrRespVo){
		attrService.updateAttrResp(attrRespVo);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		//attrService.removeByIds(Arrays.asList(attrIds));
        attrService.removeAndRelationByIds(Arrays.asList(attrIds));
        return R.ok();
    }
    ///product/attr/base/listforspu/{spuId}
    /**
     * 获取spu规格
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R listforspu(@PathVariable Long spuId){
        List<ProductAttrValueEntity> data = attrValueService.listforspuBySpuId(spuId);
        return R.ok().put("data", data);
    }
    /**
     * 修改spu规格
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable Long spuId, @RequestBody List<ProductAttrValueEntity> entities){
        attrValueService.updateSpuAttr(spuId, entities);
        return R.ok();
    }
}
