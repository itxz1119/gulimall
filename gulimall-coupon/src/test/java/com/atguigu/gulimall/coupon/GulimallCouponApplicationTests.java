package com.atguigu.gulimall.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void test() {
        // 示例 List<Map>
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(createMap(1, 10, "A"));
        list.add(createMap(2, 20, "B"));
        list.add(createMap(1, 30, "A"));
        list.add(createMap(3, 40, "C"));

        // 使用普通循环和集合操作进行合并和求和
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        for (Map<String, Object> map : list) {
            String key = map.get("x").toString() + map.get("z").toString();
            if (resultMap.containsKey(key)) {
                Map<String, Object> existingMap = resultMap.get(key);
                existingMap.put("y", (int) existingMap.get("y") + (int) map.get("y"));
            } else {
                Map<String, Object> newMap = new HashMap<>();
                newMap.put("x", map.get("x"));
                newMap.put("z", map.get("z"));
                newMap.put("y", map.get("y"));
                resultMap.put(key, newMap);
            }
        }
        result.addAll(resultMap.values());
        result.forEach(System.out::println);
    }


    private static Map<String, Object> createMap(int x, int y, String z) {
        Map<String, Object> map = new HashMap<>();
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        return map;
    }
}
