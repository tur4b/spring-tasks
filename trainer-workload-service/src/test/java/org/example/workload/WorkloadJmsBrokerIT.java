package org.example.workload;

import org.example.common.dto.WorkloadEventRequest;
import org.example.common.messaging.WorkloadMessagingConstants;
import org.example.common.model.ActionType;
import org.example.workload.document.TrainerSummaryDocument;
import org.example.workload.repository.TrainerSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Cross-service contract test: verifies the JMS message format sent by main-service's
 * WorkloadJmsProducer is correctly consumed by trainer-workload-service's WorkloadEventConsumer.
 * Uses an embedded in-process ActiveMQ broker (vm:// transport) so no external infrastructure is needed.
 */
@SpringBootTest
@ActiveProfiles({"test", "jms-test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WorkloadJmsBrokerIT {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerSummaryRepository repository;

    @BeforeEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void producerToConsumerContractRoundTrip_addEventPersisted() {
        WorkloadEventRequest req = new WorkloadEventRequest(
                "cross.svc.add", "Cross", "Add", true,
                LocalDate.of(2026, 6, 1), 90, ActionType.ADD);

        jmsTemplate.convertAndSend(WorkloadMessagingConstants.WORKLOAD_QUEUE, req);

        await().atMost(5, SECONDS)
                .until(() -> repository.findByUsername("cross.svc.add").isPresent());

        TrainerSummaryDocument doc = repository.findByUsername("cross.svc.add").orElseThrow();
        assertThat(doc.getFirstName()).isEqualTo("Cross");
        assertThat(doc.getLastName()).isEqualTo("Add");
        assertThat(doc.getYears()).hasSize(1);
        assertThat(doc.getYears().get(0).getYear()).isEqualTo(2026);
        assertThat(doc.getYears().get(0).getMonths()).hasSize(1);
        assertThat(doc.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(6);
        assertThat(doc.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(90);
    }

    @Test
    void producerToConsumerContractRoundTrip_addThenDeleteNetDuration() {
        WorkloadEventRequest add = new WorkloadEventRequest(
                "cross.svc.net", "Cross", "Net", true,
                LocalDate.of(2026, 7, 15), 120, ActionType.ADD);
        WorkloadEventRequest delete = new WorkloadEventRequest(
                "cross.svc.net", "Cross", "Net", true,
                LocalDate.of(2026, 7, 15), 40, ActionType.DELETE);

        jmsTemplate.convertAndSend(WorkloadMessagingConstants.WORKLOAD_QUEUE, add);
        await().atMost(5, SECONDS)
                .until(() -> repository.findByUsername("cross.svc.net").isPresent());

        jmsTemplate.convertAndSend(WorkloadMessagingConstants.WORKLOAD_QUEUE, delete);
        await().atMost(5, SECONDS).until(() ->
                repository.findByUsername("cross.svc.net")
                        .map(d -> d.getYears().stream()
                                .flatMap(y -> y.getMonths().stream())
                                .anyMatch(m -> m.getTrainingSummaryDuration() == 80))
                        .orElse(false));

        TrainerSummaryDocument doc = repository.findByUsername("cross.svc.net").orElseThrow();
        int duration = doc.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration();
        assertThat(duration).isEqualTo(80);
    }

    @Test
    void invalidMessage_blankUsername_notPersistedRoutedToDlq() throws InterruptedException {
        WorkloadEventRequest invalid = new WorkloadEventRequest(
                "", "Invalid", "User", true,
                LocalDate.of(2026, 8, 1), 60, ActionType.ADD);

        jmsTemplate.convertAndSend(WorkloadMessagingConstants.WORKLOAD_QUEUE, invalid);

        // consumer validates and routes to DLQ — no document should be saved
        Thread.sleep(1000);

        assertThat(repository.findByUsername("")).isEmpty();
        assertThat(repository.count()).isZero();
    }
}