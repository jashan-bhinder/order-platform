package com.jashan.orderservice.config;

import com.jashan.orderservice.event.OrderCreated;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(OrderCreated.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}


//hello