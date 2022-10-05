package kr.njw.gripp.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitConfig {
    public static final String VIDEO_PROCESSOR_DLX_KEY = "video-processor.dlx";
    public static final String VIDEO_PROCESSOR_QUEUE_KEY = "video-processor";
    public static final String VIDEO_PROCESSOR_RETURN_QUEUE_KEY = "video-processor-return";

    @Bean
    public Queue videoProcessorQueue() {
        return QueueBuilder.durable(VIDEO_PROCESSOR_QUEUE_KEY).deadLetterExchange(VIDEO_PROCESSOR_DLX_KEY).build();
    }

    @Bean
    public Queue videoProcessorReturnQueue() {
        return new Queue(VIDEO_PROCESSOR_RETURN_QUEUE_KEY);
    }

    @Bean
    public Exchange videoProcessorDeadLetterExchange() {
        return ExchangeBuilder.directExchange(VIDEO_PROCESSOR_DLX_KEY).build();
    }

    @Bean
    public Binding videoProcessorBinding(Queue videoProcessorQueue, Exchange videoProcessorDeadLetterExchange) {
        return BindingBuilder.bind(videoProcessorQueue)
                .to(videoProcessorDeadLetterExchange)
                .with(videoProcessorQueue.getActualName())
                .noargs();
    }

    @Bean
    @Primary
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public MessageConverter simpleMessageConverter() {
        return new SimpleMessageConverter();
    }
}
