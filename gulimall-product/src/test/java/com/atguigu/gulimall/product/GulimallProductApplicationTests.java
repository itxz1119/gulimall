package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;

@Slf4j
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
   public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");
        System.out.println(brandService.save(brandEntity));
    }

    @Test
    public void testLong(){
        Long a = 1000L;
        Long b = 1200L;
        System.out.println(a.equals(b));
    }
    @Test
    public void testParent(){
        Long[] parent = categoryService.findParent(225L);
        log.info("完整的父路径{}", Arrays.asList(parent));
    }

    @Test
    public void testRedis(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        //保存
        ops.set("hello", "world");
        //查询
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Autowired
    private RedissonClient redissonClient;
    @Test
    public void testRedisson(){
        System.out.println(redissonClient);
    }


}
