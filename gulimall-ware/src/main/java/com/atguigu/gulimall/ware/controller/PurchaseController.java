package com.atguigu.gulimall.ware.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 采购信息
 *
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 11:37:37
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchaseService.save(purchase);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 查询 新建 或 已分配的采购单
     * @param params
     * @return
     */
    @GetMapping("/unreceive/list")
    public R unreceiveList(Map<String, Object> params){
        PageUtils page = purchaseService.unreceiveList(params);
        return R.ok().put("page", page);
    }

    /**
     * 合并采购需求
     * @param mergeVo
     * @return
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo){
       purchaseService.mergePurchase(mergeVo);
        return R.ok();
    }

    /**
     * 领取采购单
     * @param ids
     * @return
     */
    @PostMapping("/received")
    public R receive(@RequestBody List<Long> ids){
        purchaseService.receive(ids);
        return R.ok();
    }

    /**
     * 完成采购
     */
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVo purchaseDoneVo){
        purchaseService.finish(purchaseDoneVo);
        return R.ok();
    }

}
