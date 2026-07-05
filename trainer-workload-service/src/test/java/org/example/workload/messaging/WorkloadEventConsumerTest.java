package org.example.workload.messaging;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.ActionType;
import org.example.workload.service.WorkloadCalculationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadEventConsumer Unit Tests")
class WorkloadEventConsumerTest {

    @Mock
    private WorkloadCalculationService workloadCalculationService;

    @Mock
    private WorkloadMessageValidator workloadMessageValidator;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private MessageConverter messageConverter;

    @Mock
    private Message message;

    @InjectMocks
    private WorkloadEventConsumer workloadEventConsumer;

    @Test
    @DisplayName("Valid message is processed")
    void consume_ValidMessage_ProcessesEvent() throws JMSException {
        WorkloadEventRequest request = request();
        when(messageConverter.fromMessage(message)).thenReturn(request);
        when(workloadMessageValidator.isValid(request)).thenReturn(true);

        workloadEventConsumer.consume(message);

        verify(workloadCalculationService).processEvent(request);
        verify(jmsTemplate, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Invalid message is sent to DLQ")
    void consume_InvalidMessage_SendsToDlq() throws JMSException {
        WorkloadEventRequest request = request();
        when(messageConverter.fromMessage(message)).thenReturn(request);
        when(workloadMessageValidator.isValid(request)).thenReturn(false);
        when(workloadMessageValidator.validationErrors(request)).thenReturn("Trainer username can't be blank");

        workloadEventConsumer.consume(message);

        verify(workloadCalculationService, never()).processEvent(any());
        verify(jmsTemplate).send(anyString(), any());
    }

    @Test
    @DisplayName("Conversion failure is sent to DLQ")
    void consume_ConversionFailure_SendsToDlq() throws JMSException {
        when(messageConverter.fromMessage(message)).thenThrow(new MessageConversionException("Cannot convert"));

        workloadEventConsumer.consume(message);

        verify(workloadCalculationService, never()).processEvent(any());
        verify(jmsTemplate).send(anyString(), any());
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
