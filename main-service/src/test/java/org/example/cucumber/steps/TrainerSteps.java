package org.example.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.example.cucumber.ScenarioState;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.request.UpdateStatusRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class TrainerSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScenarioState state;

    @When("I register a trainer with firstName {string} lastName {string} specializationId {int}")
    public void registerTrainer(String firstName, String lastName, int specializationId) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TrainerCreateRequest(firstName, lastName, specializationId))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I register a trainer with firstName {string} lastName {string} without specialization")
    public void registerTrainerWithoutSpecialization(String firstName, String lastName) throws Exception {
        String body = "{\"firstName\":\"" + firstName + "\",\"lastName\":\"" + lastName + "\"}";
        MockHttpServletResponse response = mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @Given("a trainer is registered with firstName {string} lastName {string} specializationId {int}")
    public void givenTrainerRegistered(String firstName, String lastName, int specializationId) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TrainerCreateRequest(firstName, lastName, specializationId))))
                .andReturn().getResponse();
        String body = response.getContentAsString();
        state.setRegisteredTrainerUsername(JsonPath.read(body, "$.data.username"));
        if (state.getJwtToken() == null) {
            state.setJwtToken(response.getHeader("X-Auth-Token"));
            state.setRegisteredUsername(state.getRegisteredTrainerUsername());
            state.setRegisteredPassword(JsonPath.read(body, "$.data.password"));
        }
    }

    @When("I request the trainer profile for the registered trainer")
    public void getRegisteredTrainerProfile() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/trainers/" + state.getRegisteredTrainerUsername())
                        .header("Authorization", "Bearer " + state.getJwtToken()))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I request the trainer profile for username {string} without a token")
    public void getTrainerProfileWithoutToken(String username) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/trainers/" + username))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @Given("I am authenticated as a registered trainer with firstName {string} lastName {string} specializationId {int}")
    public void authenticatedTrainer(String firstName, String lastName, int specializationId) throws Exception {
        givenTrainerRegistered(firstName, lastName, specializationId);
    }

    @When("I request the trainer profile for username {string}")
    public void getTrainerProfileByUsername(String username) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/trainers/" + username)
                        .header("Authorization", "Bearer " + state.getJwtToken()))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I deactivate the registered trainer")
    public void deactivateRegisteredTrainer() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(patch("/trainers/status")
                        .header("Authorization", "Bearer " + state.getJwtToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateStatusRequest(state.getRegisteredTrainerUsername(), false))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I request trainers not assigned to the registered trainee")
    public void getTrainersNotAssignedToTrainee() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        get("/trainers/not-assigned-on-trainee/" + state.getRegisteredTraineeUsername())
                                .header("Authorization", "Bearer " + state.getJwtToken()))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @And("the trainer profile firstName is {string}")
    public void trainerProfileFirstName(String expected) {
        String actual = JsonPath.read(state.getLastResponseBody(), "$.data.firstName");
        assertThat(actual).isEqualTo(expected);
    }

    @And("the trainer profile lastName is {string}")
    public void trainerProfileLastName(String expected) {
        String actual = JsonPath.read(state.getLastResponseBody(), "$.data.lastName");
        assertThat(actual).isEqualTo(expected);
    }
}