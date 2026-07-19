package org.example.client;

import jakarta.jms.Message;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.logging.TransactionContext;
import org.example.common.messaging.WorkloadMessagingConstants;
import org.example.common.model.ActionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadJmsProducer Unit Tests")
class WorkloadJmsProducerTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private WorkloadJmsProducer producer;

    @AfterEach
    void clearMdc() {
        MDC.remove(TransactionContext.TRANSACTION_MDC_KEY);
    }

    @Test
    @DisplayName("send calls jmsTemplate with the correct queue and request")
    void send_CallsJmsTemplateWithCorrectQueueAndRequest() {
        WorkloadEventRequest request = request();

        producer.send(request);

        verify(jmsTemplate).convertAndSend(
                eq(WorkloadMessagingConstants.WORKLOAD_QUEUE),
                eq(request),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    @DisplayName("send sets transaction header on JMS message when MDC contains transactionId")
    void send_SetsTransactionHeader_WhenMdcHasTransactionId() throws Exception {
        MDC.put(TransactionContext.TRANSACTION_MDC_KEY, "tx-abc-123");

        producer.send(request());

        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(
                any(String.class),
                any(),
                processorCaptor.capture()
        );

        Message mockMessage = mock(Message.class);
        processorCaptor.getValue().postProcessMessage(mockMessage);

        verify(mockMessage).setStringProperty(TransactionContext.TRANSACTION_HEADER, "tx-abc-123");
    }

    @Test
    @DisplayName("send does not set transaction header when MDC has no transactionId")
    void send_DoesNotSetTransactionHeader_WhenMdcIsEmpty() throws Exception {
        producer.send(request());

        ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(any(String.class), any(), processorCaptor.capture());

        Message mockMessage = mock(Message.class);
        processorCaptor.getValue().postProcessMessage(mockMessage);

        verify(mockMessage, org.mockito.Mockito.never())
                .setStringProperty(eq(TransactionContext.TRANSACTION_HEADER), any());
    }

    @Test
    @DisplayName("send re-throws JmsException when broker is unavailable")
    void send_ReThrowsJmsException_WhenBrokerUnavailable() {
        doThrow(new UncategorizedJmsException("Connection refused"))
                .when(jmsTemplate).convertAndSend(any(String.class), any(), any(MessagePostProcessor.class));

        assertThatThrownBy(() -> producer.send(request()))
                .isInstanceOf(UncategorizedJmsException.class)
                .hasMessageContaining("Connection refused");
    }

    private WorkloadEventRequest request() {
        return new WorkloadEventRequest(
                "trainer.one",
                "John",
                "Smith",
                true,
                LocalDate.of(2026, 6, 15),
                60,
                ActionType.ADD
        );
    }
}