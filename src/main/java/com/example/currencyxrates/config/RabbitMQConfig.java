package com.example.currencyxrates.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue exchangeRateQueue() {
        return QueueBuilder.durable("exchangeRateQueue")
                .withArgument("x-dead-letter-exchange", "dlx-exchange")
                .build();
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("exchangeRateExchange");
    }

    @Bean
    public Binding binding(Queue exchangeRateQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(exchangeRateQueue)
                .to(exchange)
                .with("exchangeRate.routing.key");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.example.currencyxrates.model");
        converter.setClassMapper(classMapper);
        return converter;
    }

}
