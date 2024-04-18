package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    void contextLoads() {

    }

    /**
     * 发送消息
     * 注意，需要把@RabbitListener注释掉，不然会自动消费消息
     */
    @Test
    void sendMsg(){
        OrderReturnReasonEntity orderEntity = new OrderReturnReasonEntity();
        orderEntity.setId(1001L);
        orderEntity.setName("zhangsan");
        rabbitTemplate.convertAndSend("hello_exchange", "hello.add", orderEntity);
        log.info("发送成功");
    }

}
