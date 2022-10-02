package kr.njw.gripp.global.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitConfig {
    public static final String VIDEO_PROCESSOR_QUEUE_KEY = "video-processor";
    public static final String VIDEO_PROCESSOR_RETURN_QUEUE_KEY = "video-processor-return";

    @Bean
    public Queue videoProcessorQueue() {
        return new Queue(VIDEO_PROCESSOR_QUEUE_KEY);
    }

    @Bean
    public Queue videoProcessorReturnQueue() {
        return new Queue(VIDEO_PROCESSOR_RETURN_QUEUE_KEY);
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
