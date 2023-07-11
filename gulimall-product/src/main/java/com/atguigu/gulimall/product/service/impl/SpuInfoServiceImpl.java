package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.Attrs;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 大保存
     * 添加商品
     * todo 更多细节 高级部分完善
     *
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1.保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfo = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfo);
        spuInfo.setCreateTime(new Date());
        spuInfo.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfo);

        //2.保存spu的商品描述信息 pms_spu_info_desc
        List<String> decriptList = vo.getDecript();
        SpuInfoDescEntity spuInfoDesc = new SpuInfoDescEntity();
        //上一步添加完成之后，主键自动回填， 这个id才不为null
        spuInfoDesc.setSpuId(spuInfo.getId());
        spuInfoDesc.setDecript(String.join(",", decriptList));
        spuInfoDescService.saveSpuDesc(spuInfoDesc);

        //3.保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfo.getId(), images);

        //4.保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueList = baseAttrs.stream().map(item -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(item.getAttrId());
            valueEntity.setAttrValue(item.getAttrValues());
            //是否快速展示
            valueEntity.setQuickShow(item.getShowDesc());
            valueEntity.setSpuId(spuInfo.getId());
            AttrEntity byId = attrService.getById(item.getAttrId());
            valueEntity.setAttrName(byId.getAttrName());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(productAttrValueList);

        //5.保存spu的积分信息 跨库操作 gulimall_sms --》sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        spuBoundTo.setSpuId(spuInfo.getId());
        BeanUtils.copyProperties(bounds, spuBoundTo);
        R r1 = couponFeignService.saveSpuBound(spuBoundTo);
        if (r1.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //6.保存当前spu对应的sku信息
        List<Skus> skusList = vo.getSkus();
        if (!skusList.isEmpty()) {
            skusList.forEach(item -> {
                //查找默认的图片
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    //DefaultImg属性值为1 表示默认图片 默认图片只有一个
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                //private String skuName;
                //private BigDecimal price;
                //private String skuTitle;
                //private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfo.getBrandId());
                skuInfoEntity.setCatalogId(spuInfo.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfo.getId());
                //sku的默认图片
                skuInfoEntity.setSkuDefaultImg(defaultImg);

                //6.1 保存sku的基本信息 pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);
                //skuInfo自增完成 id自动回填
                Long skuId = skuInfoEntity.getSkuId();

                //6.2 保存sku的图片信息 pms_sku_images
                //开始构造sku的图片信息
                List<SkuImagesEntity> skuImagesList = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    // 0/1
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    //过滤掉没有长度的imgUrl 返回true 有用的信息
                    return StringUtils.hasLength(entity.getImgUrl());
                }).collect(Collectors.toList());
                // todo 没有图片url的 不需要保存
                skuImagesService.saveBatch(skuImagesList);

                //6.3 保存sku的销售属性 pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueList = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValue = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValue);
                    skuSaleAttrValue.setSkuId(skuId);
                    return skuSaleAttrValue;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);

                //6.4 保存sku的优惠、满减等信息
                // 跨库操作 gulimall_sms --》sms_sku_ladder（满几件表）sms_sku_full_reduction（满几元） sms_member_price（会员表）
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                //过滤垃圾数据 满0元减0元的 满0件减0折的 不添加数据库
                if (skuReductionTo.getFullCount() >= 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r = couponFeignService.saveskuReduction(skuReductionTo);
                    if (r.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }


    }

    /**
     * 1.保存spu基本信息 pms_spu_info
     *
     * @param spuInfo
     */
    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfo) {
        baseMapper.insert(spuInfo);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        /*
        * key: '华为',//检索关键字
   catelogId: 6,//三级分类id
   brandId: 1,//品牌id
   status: 0,//商品状态
        * */
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasLength(key)) {
            wrapper.and(item -> {
                item.eq("id", key).or().like("spu_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.hasLength(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.hasLength(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String status = (String) params.get("status");
        if (StringUtils.hasLength(status) && !"0".equalsIgnoreCase(status)) {
            wrapper.eq("publish_status", status);
        }
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void upProduct(Long spuId) {
        //4.根据spuid 查出attrid
        List<ProductAttrValueEntity> attrValueList = productAttrValueService.listforspuBySpuId(spuId);

        List<Long> attrIds = attrValueList.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        // 4.1 根据attrIds找出可检索的规格参数
        List<Long> searchAttrIds = attrService.getSearchTypeIdbyAttrId(attrIds);
        HashSet<Long> idSet = new HashSet<>(searchAttrIds);
        List<Attrs> attrsList = attrValueList.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            Attrs attrs = new Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        //        1 查出所有的sku信息
        List<SkuInfoEntity> skuInfoList = skuInfoService.getSkusBySpuId(spuId);

        //5. 调用远程服务 查询库存
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIdList);
            //需要是个内部类对象
            TypeReference<List<SkuStockVo>> typeReference = new TypeReference<List<SkuStockVo>>() {
            };
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
        } catch (Exception e) {
            log.error("查询库存服务失败：原因{}", e);
        }
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModelList = skuInfoList.stream().map(skuInfo -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfo, skuEsModel);
            skuEsModel.setSkuPrice(skuInfo.getPrice());
            skuEsModel.setSkuImg(skuInfo.getSkuDefaultImg());

            //2.查询品牌name
            BrandEntity brandEntity = brandService.getById(skuInfo.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());

            //3.查询分类
            CategoryEntity categoryEntity = categoryService.getById(skuInfo.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());

            //4.2 设置attrs
            skuEsModel.setAttrs(attrsList);
            //设置热度评分
            skuEsModel.setHotScore(0L);

            //5.1 设置是否有库存
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(skuInfo.getSkuId()));
            }
            return skuEsModel;
        }).collect(Collectors.toList());

        // 6. 将skuEsModelList发送给es进行保存
        R r = searchFeignService.productUp(skuEsModelList);
        //远程调用成功
        if (r.getCode() == 0){
            //修改商品的上架状态
            baseMapper.updateStatusBySpuId(spuId, 1);
        } else {
            //远程调用失败
            // TODO: 2022/11/8 重复调用问题？ 接口幂等性； 重试机制
        }
    }


}
