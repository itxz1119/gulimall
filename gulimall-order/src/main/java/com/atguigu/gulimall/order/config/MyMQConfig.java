package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;

//todo 关于mq的知识
@Configuration
public class MyMQConfig {

    /**
     * 测试延时队列，通过路由键order.create.order发送到order.delay.queue（延时队列），
     * 过期后，通过路由键order.release.order发送到order.release.queue
     */
    @RabbitListener(queues = {"order.release.queue"})
    public void test(Message message, OrderEntity order, Channel channel) {
        System.out.println("收到消息：" + order.getOrderSn());
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 延时队列
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        //设置队列属性
        // x-dead-letter-exchange: order-event-exchange
        // x-dead-letter-routing-key: order.release.order
        // x-message-ttl: 60000
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        //过期时间，毫秒为单位
        arguments.put("x-message-ttl", 30000);
        //参数：name，是否持久化，是否排他，是否自动删除，队列属性
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        return new Queue("order.delay.queue", true, false, false, arguments);
    }


    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue("order.release.queue", true, false, false);
    }

    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding orderCreateOrderBinding() {
        return BindingBuilder.bind(orderDelayQueue()).to(orderEventExchange()).with("order.create.order");
    }

    @Bean
    public Binding orderReleaseOrder() {
        return BindingBuilder.bind(orderReleaseOrderQueue()).to(orderEventExchange()).with("order.release.order");
    }


}
