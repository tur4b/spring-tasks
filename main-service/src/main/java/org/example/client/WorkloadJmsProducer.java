package org.example.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.logging.TransactionContext;
import org.example.common.messaging.WorkloadMessagingConstants;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "workload.messaging.enabled", havingValue = "true", matchIfMissing = true)
public class WorkloadJmsProducer implements WorkloadMessageSender {

    private final JmsTemplate jmsTemplate;

    @Override
    public void send(WorkloadEventRequest request) {
        log.info("[transactionId={}] OPERATION sendWorkloadMessage action={} trainer={}",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                request.actionType(),
                request.trainerUsername());

        jmsTemplate.convertAndSend(WorkloadMessagingConstants.WORKLOAD_QUEUE, request, message -> {
            String transactionId = MDC.get(TransactionContext.TRANSACTION_MDC_KEY);
            if (transactionId != null) {
                message.setStringProperty(TransactionContext.TRANSACTION_HEADER, transactionId);
            }
            return message;
        });
    }
}
