package com.atguigu.gulimall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * 1.整合redisson
 * 依赖：
 * <dependency>
 * <groupId>org.redisson</groupId>
 * <artifactId>redisson</artifactId>
 * <version>3.12.0</version>
 * </dependency>
 * 在容器中放入RedissonClient即可，实例在web下的IndexController里面，配置在config包下；
 * 应用在 CategoryServiceImpl
 * 2.SpringCache
 * a:依赖：
 * <dependency>
 * <groupId>org.springframework.boot</groupId>
 * <artifactId>spring-boot-starter-cache</artifactId>
 * </dependency>
 * 还需要 spring-boot-starter-data-redis
 * b:配置和使用--》在配置文件中，spring cache type选择redis
 * 注解：@Cacheable--》触发将数据保存在缓存的操作；
 *      @CacheEvict--》触发将数据从缓存删除的操作；
 *      @CachePut--》不影响方法执行更新缓存；
 *      @Caching--》组合以上多个操作；
 *      @CacheConfig--》在类级别共享缓存的配置；
 *      第一步：在启动类开启缓存注解 @EnableCaching
 *      第二步：在方法上使用相应注解即可
 */
@EnableOpenApi
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
//@EnableCaching 可以放在配置类中
@SpringBootApplication
public class GulimallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }
}
