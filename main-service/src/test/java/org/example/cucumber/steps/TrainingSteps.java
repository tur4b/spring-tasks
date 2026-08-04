package org.example.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.example.cucumber.ScenarioState;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.request.TraineeUpdateTrainersRequest;
import org.example.dto.request.TraineeUpdateTrainersRequest.TrainerUsernameDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Slf4j
public class TrainingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScenarioState state;

    @And("the trainer is assigned to the trainee")
    public void assignTrainerToTrainee() throws Exception {
        log.debug("[Step] PUT /trainees/{}/trainers assigning trainer={}", state.getRegisteredTraineeUsername(), state.getRegisteredTrainerUsername());
        mockMvc.perform(put("/trainees/" + state.getRegisteredTraineeUsername() + "/trainers")
                        .header("Authorization", "Bearer " + state.getJwtToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeUpdateTrainersRequest(state.getRegisteredTraineeUsername(),
                                        List.of(new TrainerUsernameDTO(state.getRegisteredTrainerUsername()))))))
                .andReturn();
    }

    @When("I create a training with name {string} date {string} duration {int}")
    public void createTraining(String name, String date, int duration) throws Exception {
        log.debug("[Step] POST /trainings name={} date={} duration={}", name, date, duration);
        MockHttpServletResponse response = mockMvc.perform(post("/trainings")
                        .header("Authorization", "Bearer " + state.getJwtToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TrainingCreateRequest(
                                state.getRegisteredTraineeUsername(),
                                state.getRegisteredTrainerUsername(),
                                name,
                                LocalDate.parse(date),
                                duration))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
        log.debug("[Step] POST /trainings → status={}", state.getLastStatus());
    }

    @When("I create a training without authentication name {string} date {string} duration {int}")
    public void createTrainingWithoutAuth(String name, String date, int duration) throws Exception {
        log.debug("[Step] POST /trainings (no auth) name={} date={} duration={}", name, date, duration);
        MockHttpServletResponse response = mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TrainingCreateRequest(
                                state.getRegisteredTraineeUsername(),
                                state.getRegisteredTrainerUsername(),
                                name,
                                LocalDate.parse(date),
                                duration))))
                .andReturn().getResponse();
        state.setLastStatus(response.getStatus());
        state.setLastResponseBody(response.getContentAsString());
        log.debug("[Step] POST /trainings (no auth) → status={}", state.getLastStatus());
    }
}