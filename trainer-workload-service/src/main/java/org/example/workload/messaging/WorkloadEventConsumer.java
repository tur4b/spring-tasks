package org.example.workload.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.logging.TransactionContext;
import org.example.common.messaging.WorkloadMessagingConstants;
import org.example.workload.service.WorkloadCalculationService;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "workload.messaging.enabled", havingValue = "true", matchIfMissing = true)
public class WorkloadEventConsumer {

    private final WorkloadCalculationService workloadCalculationService;
    private final WorkloadMessageValidator workloadMessageValidator;
    private final JmsTemplate jmsTemplate;
    private final MessageConverter messageConverter;

    @JmsListener(destination = WorkloadMessagingConstants.WORKLOAD_QUEUE)
    public void consume(Message message) {
        String transactionId = readTransactionId(message);
        if (transactionId != null) {
            MDC.put(TransactionContext.TRANSACTION_MDC_KEY, transactionId);
        }

        try {
            WorkloadEventRequest request = deserialize(message);
            if (request == null) {
                String reason = "Message payload is missing or could not be deserialized";
                log.warn("[transactionId={}] OPERATION workloadMessageRejected reason={}",
                        MDC.get(TransactionContext.TRANSACTION_MDC_KEY), reason);
                sendToDeadLetterQueue(message, reason);
                return;
            }

            if (!workloadMessageValidator.isValid(request)) {
                String reason = workloadMessageValidator.validationErrors(request);
                log.warn("[transactionId={}] OPERATION workloadMessageRejected reason={}",
                        MDC.get(TransactionContext.TRANSACTION_MDC_KEY), reason);
                sendToDeadLetterQueue(message, reason);
                return;
            }

            log.info("[transactionId={}] OPERATION consumeWorkload action={} trainer={}",
                    MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                    request.actionType(),
                    request.trainerUsername());
            workloadCalculationService.processEvent(request);
        } finally {
            MDC.remove(TransactionContext.TRANSACTION_MDC_KEY);
        }
    }

    private WorkloadEventRequest deserialize(Message message) {
        try {
            Object payload = messageConverter.fromMessage(message);
            if (payload instanceof WorkloadEventRequest request) {
                return request;
            }
            return null;
        } catch (JMSException | MessageConversionException exception) {
            log.warn("[transactionId={}] OPERATION workloadMessageRejected reason={}",
                    MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                    exception.getMessage());
            return null;
        }
    }

    private void sendToDeadLetterQueue(Message originalMessage, String reason) {
        jmsTemplate.send(WorkloadMessagingConstants.WORKLOAD_DLQ, session -> {
            TextMessage deadLetterMessage = session.createTextMessage(readMessageText(originalMessage));
            deadLetterMessage.setStringProperty("deadLetterReason", reason);
            String transactionId = MDC.get(TransactionContext.TRANSACTION_MDC_KEY);
            if (transactionId != null) {
                deadLetterMessage.setStringProperty(TransactionContext.TRANSACTION_HEADER, transactionId);
            }
            return deadLetterMessage;
        });
    }

    private String readMessageText(Message message) {
        if (message instanceof TextMessage textMessage) {
            try {
                return textMessage.getText();
            } catch (JMSException exception) {
                log.warn("Failed to read JMS message text: {}", exception.getMessage());
            }
        }
        return String.valueOf(message);
    }

    private String readTransactionId(Message message) {
        try {
            return message.getStringProperty(TransactionContext.TRANSACTION_HEADER);
        } catch (JMSException exception) {
            log.debug("No transaction id on JMS message: {}", exception.getMessage());
            return null;
        }
    }
}
