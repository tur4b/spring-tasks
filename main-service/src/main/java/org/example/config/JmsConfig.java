package org.example.config;

import jakarta.jms.Queue;
import org.apache.activemq.command.ActiveMQQueue;
import org.example.common.messaging.WorkloadMessagingConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@ConditionalOnProperty(name = "workload.messaging.enabled", havingValue = "true", matchIfMissing = true)
public class JmsConfig {

    @Bean
    public Queue workloadQueue() {
        return new ActiveMQQueue(WorkloadMessagingConstants.WORKLOAD_QUEUE);
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
