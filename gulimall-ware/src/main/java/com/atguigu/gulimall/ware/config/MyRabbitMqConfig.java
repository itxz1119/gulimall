package com.atguigu.gulimall.ware.config;

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

import java.util.HashMap;

@Configuration
public class MyRabbitMqConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @RabbitListener(queues = {"stock.release.stock.queue"})
    public void test(Message message) {

    }
    /**
     * 延时队列
     * @return
     */
    @Bean
    public Queue stockDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        //过期时间，毫秒为单位
        arguments.put("x-message-ttl", 60000);
        //参数：name，是否持久化，是否排他，是否自动删除，队列属性
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }


    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public TopicExchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Binding stockLockedBinding() {
        return BindingBuilder.bind(stockDelayQueue()).to(stockEventExchange()).with("stock.locked");
    }

    @Bean
    public Binding stockReleaseBinding() {
        return BindingBuilder.bind(stockReleaseStockQueue()).to(stockEventExchange()).with("stock.release.#");
    }


}
