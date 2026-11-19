package org.dealer.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RabbitMqConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";

    public static final String ORDER_SENT_FOR_APPROVAL_QUEUE = "order.sent-for-approval.queue";
    public static final String ORDER_SENT_FOR_APPROVAL_ROUTING_KEY = "order.sent-for-approval";

    public static final String ORDER_APPROVED_QUEUE = "order.approved.queue";
    public static final String ORDER_APPROVED_ROUTING_KEY = "order.approved";

    public static final String ORDER_REJECTED_QUEUE = "order.rejected.queue";
    public static final String ORDER_REJECTED_ROUTING_KEY = "order.rejected";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderSentForApprovalQueue() {
        return new Queue(ORDER_SENT_FOR_APPROVAL_QUEUE, true);
    }

    @Bean
    public Binding orderSentForApprovalBinding(
            Queue orderSentForApprovalQueue,
            DirectExchange orderExchange
    ) {
        return BindingBuilder
                .bind(orderSentForApprovalQueue)
                .to(orderExchange)
                .with(ORDER_SENT_FOR_APPROVAL_ROUTING_KEY);
    }

    @Bean
    public Queue orderApprovedQueue() {
        return new Queue(ORDER_APPROVED_QUEUE, true);
    }

    @Bean
    public Binding orderApprovedBinding(
            Queue orderApprovedQueue,
            DirectExchange orderExchange
    ) {
        return BindingBuilder
                .bind(orderApprovedQueue)
                .to(orderExchange)
                .with(ORDER_APPROVED_ROUTING_KEY);
    }

    @Bean
    public Queue orderRejectedQueue() {
        return new Queue(ORDER_REJECTED_QUEUE, true);
    }

    @Bean
    public Binding orderRejectedBinding(
            Queue orderRejectedQueue,
            DirectExchange orderExchange
    ) {
        return BindingBuilder
                .bind(orderRejectedQueue)
                .to(orderExchange)
                .with(ORDER_REJECTED_ROUTING_KEY);
    }
}
