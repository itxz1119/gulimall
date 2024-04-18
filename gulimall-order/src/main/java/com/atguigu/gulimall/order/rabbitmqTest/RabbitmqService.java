package com.atguigu.gulimall.order.rabbitmqTest;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 订单副服务启动多个，同一个消息只能一个客户端收到。
 * 一条消息处理完成，方法运行结束，才会接收下一条消息
 */
@Slf4j
@Service
//该注解用在类上，配合@RabbitHandler注解可以按照消息的对象类型定向接收；放在方法上代表只有该方法接收
@RabbitListener(queues = {"hello_queue"})
public class RabbitmqService {

    /**
     * 自定义消息转换器，就不可以用string接收，不然会报错。
     * 可以不使用消息转换器，手动转成json
     *
     * @param message                    里面封装了消息的详细信息
     * @param orderReturnReasonEntityMsg 这是自己发送的消息
     * @param channel
     */
    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity orderReturnReasonEntityMsg, Channel channel) {
        log.info("收到的消息：{}", orderReturnReasonEntityMsg);
        /**
         * 消费端确认，自动ack，如果服务宕机，消息会丢失
         *  手动ack，只要未确认，服务宕机，消息会回到read状态
         *  basicAck：确认收货
         *  第二个参数传false，说明非批量签收
         *  basicNack：拒收
         *  第三个参数：true代表重新加入到队列中， false将消息丢弃
         */
        try {
            //通道内自增id，比如消息有五条，从1开始依次排序
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            channel.basicAck(deliveryTag, false);
//            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitHandler
    public void receiveMessage(OrderEntity message, Channel channel) {
        log.info("收到的消息2：{}", message);
    }
}
