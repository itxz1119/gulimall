package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneItemVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService detailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils unreceiveList(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.in("status", "0", "1");
        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseEnum.CREATE.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;

        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity detail = new PurchaseDetailEntity();
            detail.setId(item);
            detail.setPurchaseId(finalPurchaseId);
            detail.setStatus(WareConstant.PurchaseDetailEnum.ASSIGNED.getCode());
            return detail;
        }).collect(Collectors.toList());
        detailService.updateBatchById(collect);

        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setUpdateTime(new Date());
        purchase.setId(finalPurchaseId);
        this.updateById(purchase);
    }

    @Override
    public void receive(List<Long> ids) {
        //1.确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchaseEnum.CREATE.getCode() ||
                    item.getStatus() == WareConstant.PurchaseEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseEnum.RECEIVE.getCode());
            return item;
        }).collect(Collectors.toList());
        //2.改变采购单的id
        this.updateBatchById(collect);

        //3.改变采购需求状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> list = detailService.listByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect1 = list.stream().map(detail -> {
                PurchaseDetailEntity entity = new PurchaseDetailEntity();
                entity.setId(detail.getId());
                entity.setStatus(WareConstant.PurchaseDetailEnum.BUYING.getCode());
                return entity;
            }).collect(Collectors.toList());
            detailService.updateBatchById(collect1);
        });
    }

    @Override
    public void finish(PurchaseDoneVo doneVo) {
        Long id = doneVo.getId();

        //2.修改采购需求的id
        boolean flag = true;
        List<PurchaseDoneItemVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> detailList = new ArrayList<>();
        for (PurchaseDoneItemVo item : items) {
            PurchaseDetailEntity detail = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailEnum.HASERROR.getCode()) {
                flag = false;
                detail.setStatus(item.getStatus());
            } else {
                detail.setStatus(WareConstant.PurchaseDetailEnum.FINISH.getCode());
                //3.采购成功，进行入库
                PurchaseDetailEntity byId = detailService.getById(item.getItemId());
                wareSkuService.addStock(byId.getSkuId(), byId.getWareId(), byId.getSkuNum());
            }
            detail.setId(item.getItemId());
            detailList.add(detail);
        }
        detailService.updateBatchById(detailList);

        //1.修改采购单的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseEnum.FINISH.getCode() : WareConstant.PurchaseEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);
    }

}
