package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author zxy
 * @email zxy@gmail.com
 * @date 2022-09-27 09:45:09
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /*
     * 获取属性分组关联的属性
     * 10、/product/attrgroup/{attrgroupId}/attr/relation
     * */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable Long attrgroupId) {
        List<AttrEntity> attrList = attrService.getRelation(attrgroupId);
        return R.ok().put("data", attrList);
    }


    /**
     * 属性分组中的关联功能
     *  /product/attrgroup/{attrgroupId}/noattr/relation
     *  获取本分类（category）下没有被关联的规格参数
     * @param attrgroupId
     * @param params
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable Long attrgroupId,
                            @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelation(attrgroupId, params);
        return R.ok().put("page", page);
    }

    /**
     * 添加属性分组的关联
     * @param relationEntityList
     * @return
     */
    @PostMapping("/attr/relation")
    public R relation(@RequestBody List<AttrAttrgroupRelationEntity> relationEntityList){
        relationService.saveBatch(relationEntityList);
        return R.ok();
    }
    /*
    * 删除属性关联关系
    * 12、/product/attrgroup/attr/relation/delete
    * */
    @PostMapping("/attr/relation/delete")
    public R deleteAttrRelation(@RequestBody AttrAttrgroupRelationEntity[] vos ){
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable Long catelogId) {
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        // 找到该子节点的完整节点
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.findParent(catelogId);
        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));
        return R.ok();
    }

    /**
     * 根据分类id --》查出属性分组 --》 查出规格参数
     * @param catelogId
     * @return
     */
    @GetMapping("/{catelogId}/withattr")
    public R getGroupWithAttr(@PathVariable Long catelogId){
        List<AttrGroupWithAttrVo> attrGroupWithAttrVoList = attrGroupService.getAttrGroupWithAttr(catelogId);
        return R.ok().put("data",attrGroupWithAttrVoList);
    }

}
