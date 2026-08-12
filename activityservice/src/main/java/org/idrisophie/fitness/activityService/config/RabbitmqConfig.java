package org.idrisophie.fitness.activityService.config;

import org.springframework.context.annotation.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitmqConfig {

    @Bean
    public Queue activityQueue() {
        return new Queue("activity.queue", true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
