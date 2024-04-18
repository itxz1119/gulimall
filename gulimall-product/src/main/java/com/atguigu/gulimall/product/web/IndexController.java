package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        //查出所有的一级分类
        List<CategoryEntity> categoryList = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categoryList);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    /**
     * redisson看门狗
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //获取同一把锁，只要名字一样就是同一把锁
        //1.在redis里面加入一个key为my-lock的锁。释放锁之后，自动删除
        RLock lock = redisson.getLock("my-lock");
        /*2.加锁--默认加锁时长为30s
         * 1)、未指定占锁时间
         * 锁的自动续期--如果业务超长，运行期间自动给锁续上新的30s；
         * 占锁成功，启动一个定时任务，每过看门狗时间的1/3，自动续期
         * 2)、加锁的业务只要完成，就不会给当前业务续期，即使不手动解锁，锁默认在30s之后自动删除
         */
        //lock.lock(); //阻塞式等待，如果获取不到锁，就一直等待。原来的方式是如果加锁失败，自旋的方式。
        lock.lock(10, TimeUnit.SECONDS); //10s自动解锁，不会自动续期--调用的redis执行脚本
        try {
            System.out.println("加锁成功" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            System.out.println("释放锁" + Thread.currentThread().getId());
            //释放锁
            lock.unlock();
        }
        return "hello";
    }

    /**
     * 写锁
     * 读写锁：保证读到的数据都是最新的。在写业务未执行完之前，不可以访问读操作
     * 写 + 读：等待写操作完成；
     * 读 + 写：等待读操作完成；
     * 写 + 写：阻塞状态；
     * 读 + 读：相当于无锁
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        String s = "";
        try {
            //1.改数据加写锁，读数据加读锁
            rLock.lock();
            s = UUID.randomUUID().toString(); //相当于业务中的更新操作
            Thread.sleep(30000);
            stringRedisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
        }
        return s;
    }

    /**
     * 读锁
     */
    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            s = stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
        }
        return s;
    }

    /**
     * 闭锁
     * eg：放假了，五个班的人都走了，关闭大门
     */
    @ResponseBody
    @GetMapping("lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();//等待闭锁都完成
        return "放假了。。。";
    }

    @ResponseBody
    @GetMapping("go/{id}")
    public String go(@PathVariable Integer id) throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();//计数减一
        return id + "班的人都走了。。。";
    }

    /**
     * 车库停车---3个车位
     */
    @ResponseBody
    @GetMapping("park")
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        //尝试获取一个车位，如果没有车位，返回false。如果括号里面有值为3，代表只能占3次，即使车位有10个，也只能占3个
        //park.acquire(); //占一个车位,如果没有车位，就一直等待
        boolean b = park.tryAcquire();
        return "ok-->" + b;
    }

    @ResponseBody
    @GetMapping("gogo")
    public String gogo() {
        RSemaphore park = redisson.getSemaphore("park");
        park.release(); //释放一个车位
        return "ok";
    }

}
