package org.example.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.cucumber.ScenarioState;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.UpdateStatusRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class TraineeSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScenarioState state;

    @When("I register a trainee with firstName {string} lastName {string}")
    public void registerTrainee(String firstName, String lastName) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeCreateRequest(firstName, lastName, null, null))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @Given("a trainee is registered with firstName {string} lastName {string}")
    public void givenTraineeRegistered(String firstName, String lastName) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeCreateRequest(firstName, lastName, null, null))))
                .andReturn().getResponse();
        String body = response.getContentAsString();
        state.setRegisteredTraineeUsername(JsonPath.read(body, "$.data.username"));
        state.setRegisteredPassword(JsonPath.read(body, "$.data.password"));
        state.setRegisteredUsername(state.getRegisteredTraineeUsername());
        state.setJwtToken(response.getHeader("X-Auth-Token"));
    }

    @When("I request the profile for the registered trainee")
    public void getRegisteredTraineeProfile() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/trainees/" + state.getRegisteredTraineeUsername())
                        .header("Authorization", "Bearer " + state.getJwtToken()))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I request the profile for username {string} without a token")
    public void getProfileWithoutToken(String username) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/trainees/" + username))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @Given("I am authenticated as a registered trainee with firstName {string} lastName {string}")
    public void authenticatedTrainee(String firstName, String lastName) throws Exception {
        givenTraineeRegistered(firstName, lastName);
    }

    @When("I request the profile for username {string}")
    public void getProfileByUsername(String username) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/trainees/" + username)
                        .header("Authorization", "Bearer " + state.getJwtToken()))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I delete the registered trainee")
    public void deleteRegisteredTrainee() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(delete("/trainees/" + state.getRegisteredTraineeUsername())
                        .header("Authorization", "Bearer " + state.getJwtToken()))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I deactivate the registered trainee")
    public void deactivateRegisteredTrainee() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(patch("/trainees/status")
                        .header("Authorization", "Bearer " + state.getJwtToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateStatusRequest(state.getRegisteredTraineeUsername(), false))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @Then("the response should contain a username")
    public void responseContainsUsername() {
        String username = JsonPath.read(state.getLastResponseBody(), "$.data.username");
        assertThat(username).isNotBlank();
    }

    @Then("the response should contain a password")
    public void responseContainsPassword() {
        String password = JsonPath.read(state.getLastResponseBody(), "$.data.password");
        assertThat(password).isNotBlank();
    }

    @And("the profile firstName is {string}")
    public void profileFirstName(String expected) {
        String actual = JsonPath.read(state.getLastResponseBody(), "$.data.firstName");
        assertThat(actual).isEqualTo(expected);
    }

    @And("the profile lastName is {string}")
    public void profileLastName(String expected) {
        String actual = JsonPath.read(state.getLastResponseBody(), "$.data.lastName");
        assertThat(actual).isEqualTo(expected);
    }
}