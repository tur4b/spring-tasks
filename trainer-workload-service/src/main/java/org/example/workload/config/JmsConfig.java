package org.example.workload.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.Queue;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.messaging.WorkloadMessagingConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "workload.messaging.enabled", havingValue = "true", matchIfMissing = true)
public class JmsConfig {

    @Bean
    public Queue workloadQueue() {
        return new ActiveMQQueue(WorkloadMessagingConstants.WORKLOAD_QUEUE);
    }

    @Bean
    public Queue workloadDeadLetterQueue() {
        return new ActiveMQQueue(WorkloadMessagingConstants.WORKLOAD_DLQ);
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);  // <-- uses Boot's mapper (already has JavaTimeModule)
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(Map.of(
                "WorkloadEventRequest", WorkloadEventRequest.class
        ));
        return converter;
    }

    /**
     * Configures redelivery: up to 3 retries with 2-second initial delay and
     * exponential back-off (multiplier 2.0), capped at 10 seconds per attempt.
     * After exhausting retries ActiveMQ routes the message to the DLQ automatically.
     */
    @Bean
    public ActiveMQConnectionFactoryCustomizer redeliveryPolicyCustomizer() {
        return (ActiveMQConnectionFactory factory) -> {
            RedeliveryPolicy policy = new RedeliveryPolicy();
            policy.setMaximumRedeliveries(3);
            policy.setInitialRedeliveryDelay(2_000L);
            policy.setBackOffMultiplier(2.0);
            policy.setUseExponentialBackOff(true);
            policy.setMaximumRedeliveryDelay(10_000L);
            factory.setRedeliveryPolicy(policy);
        };
    }
}
