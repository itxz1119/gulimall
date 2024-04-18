package com.atguigu.gulimall.order.rabbitmqTest;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
public class RabbitmqController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/sendMq")
    public R sendMq() {
        for (int i = 8; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity orderEntity = new OrderReturnReasonEntity();
                orderEntity.setId(1001L);
                orderEntity.setName("zhangsan");
                CorrelationData correlationData = new CorrelationData();
                correlationData.setId(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello_exchange", "hello.add", orderEntity, correlationData);
            }/* else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId(1001L);
                orderEntity.setMemberUsername("lisi");
                rabbitTemplate.convertAndSend("hello_exchange", "hello111.add", orderEntity);
            }*/
            log.info("发送成功");
        }
        return R.ok();
    }
}
