package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.dao.TrainingTypeRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TraineeUpdateTrainersRequest;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.request.TrainingCreateRequest;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("TrainingController Integration Tests")
class TrainingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    // ── unauthenticated cases ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /trainings - returns 403 when no JWT token is supplied")
    void createTraining_ReturnsForbiddenWithoutJwtToken() throws Exception {
        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TrainingCreateRequest(
                                "trainee.dummy", "trainer.dummy", "Training",
                                LocalDate.now().plusDays(1), 45))))
                .andExpect(status().isForbidden());
    }

    // ── authenticated cases ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /trainings - creates training with valid JWT token when trainer is assigned to trainee")
    void createTraining_ReturnsOkWithJwtTokenWhenRelationExists() throws Exception {
        TrainingType type = trainingTypeRepository.saveAndFlush(trainingType(TrainingTypeName.CARDIO));

        String trainerResponse = registerTrainer("John", "Doe", type.getId());
        String traineeResponse = registerTrainee("Alice", "Smith");
        String trainerUsername = JsonPath.read(trainerResponse, "$.data.username");
        String trainerPassword = JsonPath.read(trainerResponse, "$.data.password");
        String traineeUsername = JsonPath.read(traineeResponse, "$.data.username");

        String jwtToken = obtainJwtToken(trainerUsername, trainerPassword);

        mockMvc.perform(put("/trainees/{username}/trainers", traineeUsername)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TraineeUpdateTrainersRequest(
                                traineeUsername,
                                List.of(new TraineeUpdateTrainersRequest.TrainerUsernameDTO(trainerUsername))
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/trainings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TrainingCreateRequest(
                                traineeUsername, trainerUsername,
                                "Morning cardio", LocalDate.now().plusDays(1), 45))))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("POST /trainings - returns 404 with valid JWT token when trainer is not assigned to trainee")
    void createTraining_ReturnsNotFoundWithJwtTokenWhenRelationMissing() throws Exception {
        TrainingType type = trainingTypeRepository.saveAndFlush(trainingType(TrainingTypeName.STRENGTH));

        String trainerResponse = registerTrainer("John", "Doe", type.getId());
        String traineeResponse = registerTrainee("Alice", "Smith");
        String trainerUsername = JsonPath.read(trainerResponse, "$.data.username");
        String trainerPassword = JsonPath.read(trainerResponse, "$.data.password");
        String traineeUsername = JsonPath.read(traineeResponse, "$.data.username");

        String jwtToken = obtainJwtToken(trainerUsername, trainerPassword);

        mockMvc.perform(post("/trainings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TrainingCreateRequest(
                                traineeUsername, trainerUsername,
                                "Morning strength", LocalDate.now().plusDays(1), 45))))
                .andExpect(status().isNotFound());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private String registerTrainer(String firstName, String lastName, Integer specializationId) throws Exception {
        return mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TrainerCreateRequest(firstName, lastName, specializationId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private String registerTrainee(String firstName, String lastName) throws Exception {
        return mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeCreateRequest(firstName, lastName, "Main street", LocalDate.of(1995, 1, 10)))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private String obtainJwtToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.data.accessToken");
    }

    private static TrainingType trainingType(TrainingTypeName name) {
        TrainingType trainingType = new TrainingType();
        trainingType.setName(name);
        return trainingType;
    }
}
