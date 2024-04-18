package com.atguigu.gulimall.order.rabbitmqTest;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RabbitmqConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 自定义消息转换器，接收消息就不可以用string接收，不然会报错。
     * 可以不使用消息转换器，手动转成json
     * @return
     */
    /*@Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }*/

    //这个方法会在rabbitmqConfig对象创建后执行
    @PostConstruct
    public void initRabbitTemplate() {
        //设置抵达交换机的回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @param correlationData 当前消息唯一关联id
             * @param ack 消息是否收到 收到ack为true
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("抵达交换机");
            }
        });
        //消息由交换机抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             *  消息抵达队列失败才会调用
             * @param message 投递失败的消息详情
             * @param code     状态码
             * @param text      文本内容
             * @param exchange  这个消息要发送给哪个交换机
             * @param key       用的路由键
             */
            @Override
            public void returnedMessage(Message message, int code, String text, String exchange, String key) {
                System.out.println("状态码：" + code + "====text：" + text + "===交换机：" + exchange
                        + "====key:" + key);
            }
        });

    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("hello_exchange");
    }

    @Bean
    public Queue queue() {
        return new Queue("hello_queue");
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(topicExchange()).with("hello.*");
    }
}
