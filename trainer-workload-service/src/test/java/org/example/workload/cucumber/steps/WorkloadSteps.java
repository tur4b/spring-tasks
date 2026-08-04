package org.example.workload.cucumber.steps;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.example.common.security.JwtService;
import org.example.workload.cucumber.WorkloadScenarioState;
import org.example.workload.repository.TrainerSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Slf4j
public class WorkloadSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TrainerSummaryRepository repository;

    @Autowired
    private WorkloadScenarioState state;

    @Given("a valid JWT token for trainer-workload-service tests")
    public void setupJwtToken() {
        repository.deleteAll();
        state.setJwtToken(jwtService.generateToken("main-service"));
        log.debug("[Setup] JWT generated and database cleared");
    }

    @When("I add a workload for trainer {string} firstName {string} lastName {string} date {string} duration {int}")
    public void addWorkload(String username, String firstName, String lastName, String date, int duration) throws Exception {
        log.debug("[Step] POST /trainers/{}/workloads date={} duration={}", username, date, duration);
        MockHttpServletResponse response = mockMvc.perform(post("/trainers/" + username + "/workloads")
                        .header("Authorization", "Bearer " + state.getJwtToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildWorkloadBody(firstName, lastName, true, date, duration)))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
        log.debug("[Step] POST /trainers/{}/workloads → status={}", username, state.getLastStatus());
    }

    @Given("a workload exists for trainer {string} firstName {string} lastName {string} date {string} duration {int}")
    public void givenWorkloadExists(String username, String firstName, String lastName, String date, int duration) throws Exception {
        addWorkload(username, firstName, lastName, date, duration);
    }

    @When("I delete workload for trainer {string} firstName {string} lastName {string} date {string} duration {int}")
    public void deleteWorkload(String username, String firstName, String lastName, String date, int duration) throws Exception {
        log.debug("[Step] DELETE /trainers/{}/workloads date={} duration={}", username, date, duration);
        MockHttpServletResponse response = mockMvc.perform(delete("/trainers/" + username + "/workloads")
                        .header("Authorization", "Bearer " + state.getJwtToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildWorkloadBody(firstName, lastName, true, date, duration)))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
        log.debug("[Step] DELETE /trainers/{}/workloads → status={}", username, state.getLastStatus());
    }

    @When("I query monthly workload for trainer {string} year {int} month {int}")
    public void queryMonthlyWorkload(String username, int year, int month) throws Exception {
        log.debug("[Step] GET /trainers/{}/workloads/years/{}/months/{}", username, year, month);
        MockHttpServletResponse response = mockMvc.perform(
                        get("/trainers/" + username + "/workloads/years/" + year + "/months/" + month)
                                .header("Authorization", "Bearer " + state.getJwtToken()))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
        log.debug("[Step] GET monthly workload → status={} body={}", state.getLastStatus(), state.getLastResponseBody());
    }

    @When("I query full workload summary for trainer {string}")
    public void queryFullSummary(String username) throws Exception {
        log.debug("[Step] GET /trainers/{}/workloads", username);
        MockHttpServletResponse response = mockMvc.perform(
                        get("/trainers/" + username + "/workloads")
                                .header("Authorization", "Bearer " + state.getJwtToken()))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
        log.debug("[Step] GET full summary → status={}", state.getLastStatus());
    }

    @When("I add a workload without authentication for trainer {string}")
    public void addWorkloadWithoutAuth(String username) throws Exception {
        log.debug("[Step] POST /trainers/{}/workloads (no auth)", username);
        MockHttpServletResponse response = mockMvc.perform(post("/trainers/" + username + "/workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildWorkloadBody("John", "Doe", true, "2026-06-15", 60)))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
        log.debug("[Step] POST (no auth) → status={}", state.getLastStatus());
    }

    @When("I add an invalid workload for trainer {string} without required fields")
    public void addInvalidWorkload(String username) throws Exception {
        log.debug("[Step] POST /trainers/{}/workloads (invalid body)", username);
        MockHttpServletResponse response = mockMvc.perform(post("/trainers/" + username + "/workloads")
                        .header("Authorization", "Bearer " + state.getJwtToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
        log.debug("[Step] POST (invalid) → status={}", state.getLastStatus());
    }

    @Then("the workload response status should be {int}")
    public void workloadResponseStatus(int expected) {
        assertThat(state.getLastStatus()).isEqualTo(expected);
    }

    @And("the monthly duration should be {int}")
    public void monthlyDurationShouldBe(int expected) {
        Integer actual = JsonPath.read(state.getLastResponseBody(), "$.trainingSummaryDuration");
        assertThat(actual).isEqualTo(expected);
    }

    @And("the summary contains trainer username {string}")
    public void summaryContainsUsername(String expected) {
        String actual = JsonPath.read(state.getLastResponseBody(), "$.username");
        assertThat(actual).isEqualTo(expected);
    }

    private String buildWorkloadBody(String firstName, String lastName, boolean isActive, String date, int duration) {
        return """
                {
                  "trainerFirstName": "%s",
                  "trainerLastName": "%s",
                  "isActive": %b,
                  "trainingDate": "%s",
                  "trainingDuration": %d
                }
                """.formatted(firstName, lastName, isActive, date, duration);
    }
}