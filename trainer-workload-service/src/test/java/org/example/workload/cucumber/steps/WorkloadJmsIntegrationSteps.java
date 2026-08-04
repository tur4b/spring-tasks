package org.example.workload.cucumber.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.messaging.WorkloadMessagingConstants;
import org.example.common.model.ActionType;
import org.example.workload.cucumber.WorkloadScenarioState;
import org.example.workload.repository.TrainerSummaryRepository;
import org.example.workload.service.WorkloadCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Steps that drive the JMS consumer path in trainer-workload-service.
 * When messaging is enabled (jms-test profile), events are sent via JmsTemplate
 * through the embedded ActiveMQ broker so the full consumer pipeline executes:
 * deserialization → validation → WorkloadCalculationService → MongoDB.
 * When messaging is disabled (test profile), events are processed directly via
 * WorkloadCalculationService to keep the non-broker Cucumber suite fast.
 */
@Slf4j
public class WorkloadJmsIntegrationSteps {

    @Autowired
    private WorkloadCalculationService workloadCalculationService;

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerSummaryRepository repository;

    @Autowired
    private WorkloadScenarioState state;

    @Value("${workload.messaging.enabled:false}")
    private boolean messagingEnabled;

    @When("a JMS ADD event arrives for trainer {string} firstName {string} lastName {string} date {string} duration {int}")
    public void jmsAddEvent(String username, String firstName, String lastName, String date, int duration) {
        processEvent(username, firstName, lastName, date, duration, ActionType.ADD);
    }

    @And("a JMS DELETE event arrives for trainer {string} firstName {string} lastName {string} date {string} duration {int}")
    public void jmsDeleteEvent(String username, String firstName, String lastName, String date, int duration) {
        processEvent(username, firstName, lastName, date, duration, ActionType.DELETE);
    }

    @When("an invalid JMS message with blank username arrives for month {string}")
    public void invalidJmsMessage(String monthLabel) {
        log.debug("[JMS] dispatching invalid (blank username) event for month={}", monthLabel);
        WorkloadEventRequest invalid = new WorkloadEventRequest(
                "",
                "First",
                "Last",
                true,
                LocalDate.parse(monthLabel + "-01"),
                60,
                ActionType.ADD
        );
        try {
            if (messagingEnabled && jmsTemplate != null) {
                jmsTemplate.convertAndSend(WorkloadMessagingConstants.WORKLOAD_QUEUE, invalid);
                // consumer rejects invalid messages to DLQ — brief wait then verify no record persisted
                Thread.sleep(500);
            } else {
                workloadCalculationService.processEvent(invalid);
            }
            state.setJmsProcessingErrored(false);
            log.debug("[JMS] invalid message dispatched — no exception thrown");
        } catch (Exception e) {
            log.debug("[JMS] invalid message raised exception: {}", e.getMessage());
            state.setJmsProcessingErrored(true);
        }
    }

    @Then("the JMS processing should complete without errors")
    public void jmsProcessingCompletedWithoutErrors() {
        assertThat(state.isJmsProcessingErrored()).isFalse();
    }

    private void processEvent(String username, String firstName, String lastName,
                               String date, int duration, ActionType action) {
        log.debug("[JMS] dispatching {} event for trainer={} date={} duration={}", action, username, date, duration);
        LocalDate trainingDate = LocalDate.parse(date);
        WorkloadEventRequest event = new WorkloadEventRequest(
                username, firstName, lastName, true, trainingDate, duration, action
        );
        try {
            if (messagingEnabled && jmsTemplate != null) {
                int year = trainingDate.getYear();
                int month = trainingDate.getMonthValue();
                int durationBefore = currentDuration(username, year, month);
                jmsTemplate.convertAndSend(WorkloadMessagingConstants.WORKLOAD_QUEUE, event);
                if (action == ActionType.ADD) {
                    int expected = durationBefore + duration;
                    await().atMost(5, SECONDS)
                            .until(() -> currentDuration(username, year, month) == expected);
                } else {
                    int expected = Math.max(0, durationBefore - duration);
                    await().atMost(5, SECONDS)
                            .until(() -> currentDuration(username, year, month) == expected);
                }
            } else {
                workloadCalculationService.processEvent(event);
            }
            state.setJmsProcessingErrored(false);
            log.debug("[JMS] {} event for trainer={} processed successfully", action, username);
        } catch (Exception e) {
            log.debug("[JMS] {} event for trainer={} raised exception: {}", action, username, e.getMessage());
            state.setJmsProcessingErrored(true);
        }
    }

    private int currentDuration(String username, int year, int month) {
        return repository.findByUsername(username)
                .flatMap(doc -> doc.getYears().stream()
                        .filter(y -> y.getYear() == year)
                        .flatMap(y -> y.getMonths().stream())
                        .filter(m -> m.getMonth() == month)
                        .findFirst())
                .map(m -> m.getTrainingSummaryDuration())
                .orElse(0);
    }

}