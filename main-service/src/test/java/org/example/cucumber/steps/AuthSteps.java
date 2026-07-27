package org.example.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.cucumber.ScenarioState;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class AuthSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScenarioState state;

    @When("I login with the registered trainee credentials")
    public void loginWithRegisteredCredentials() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthRequest(state.getRegisteredUsername(), state.getRegisteredPassword()))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
        if (response.getStatus() == 200) {
            state.setJwtToken(JsonPath.read(response.getContentAsString(), "$.data.accessToken"));
        }
    }

    @When("I login with username and password {string}")
    public void loginWithWrongPassword(String wrongPassword) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthRequest(state.getRegisteredUsername(), wrongPassword))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I login with username {string} and password {string}")
    public void loginWithUsernameAndPassword(String username, String password) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest(username, password))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I change the password to {string}")
    public void changePasswordTo(String newPassword) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(put("/auth/change-password")
                        .header("Authorization", "Bearer " + state.getJwtToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest(state.getRegisteredUsername(),
                                        state.getRegisteredPassword(), newPassword))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I change the password for {string} oldPassword {string} newPassword {string} without token")
    public void changePasswordWithoutToken(String username, String oldPassword, String newPassword) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest(username, oldPassword, newPassword))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I logout with the current token")
    public void logoutWithCurrentToken() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + state.getJwtToken()))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
    }

    @When("I logout with the current token again")
    public void logoutWithCurrentTokenAgain() throws Exception {
        logoutWithCurrentToken();
    }

    @Then("the response should contain an access token")
    public void responseContainsAccessToken() {
        String token = JsonPath.read(state.getLastResponseBody(), "$.data.accessToken");
        assertThat(token).isNotBlank();
    }
}