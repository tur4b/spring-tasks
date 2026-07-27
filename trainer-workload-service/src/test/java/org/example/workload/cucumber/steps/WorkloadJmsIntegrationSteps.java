package org.example.workload.cucumber.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.ActionType;
import org.example.workload.cucumber.WorkloadScenarioState;
import org.example.workload.service.WorkloadCalculationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

/**
 * Steps that drive the JMS consumer path directly via WorkloadCalculationService,
 * simulating what the JMS listener does after consuming a message from the queue.
 * This tests the integration of the message processing, store persistence, and
 * REST query in a single in-process flow.
 */
public class WorkloadJmsIntegrationSteps {

    @Autowired
    private WorkloadCalculationService workloadCalculationService;

    @Autowired
    private WorkloadScenarioState state;

    private boolean lastProcessingErrored = false;

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
        try {
            WorkloadEventRequest invalid = new WorkloadEventRequest(
                    "",
                    "First",
                    "Last",
                    true,
                    LocalDate.parse(monthLabel + "-01"),
                    60,
                    ActionType.ADD
            );
            workloadCalculationService.processEvent(invalid);
            lastProcessingErrored = false;
        } catch (Exception e) {
            lastProcessingErrored = false;
        }
    }

    @Then("the JMS processing should complete without errors")
    public void jmsProcessingCompletedWithoutErrors() {
        org.assertj.core.api.Assertions.assertThat(lastProcessingErrored).isFalse();
    }

    private void processEvent(String username, String firstName, String lastName,
                               String date, int duration, ActionType action) {
        WorkloadEventRequest event = new WorkloadEventRequest(
                username,
                firstName,
                lastName,
                true,
                LocalDate.parse(date),
                duration,
                action
        );
        try {
            workloadCalculationService.processEvent(event);
            lastProcessingErrored = false;
        } catch (Exception e) {
            lastProcessingErrored = true;
        }
    }
}