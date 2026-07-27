package org.example.cucumber.steps;

import io.cucumber.java.en.Then;
import org.example.cucumber.ScenarioState;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    @Autowired
    private ScenarioState state;

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int expectedStatus) {
        assertThat(state.getLastStatus()).isEqualTo(expectedStatus);
    }
}