package com.example.currencyxrates.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.rabbitmq")
@Getter
@Setter
@Component
public class RabbitMQProperties {

    private String exchange;
    private String routingKey;

}
