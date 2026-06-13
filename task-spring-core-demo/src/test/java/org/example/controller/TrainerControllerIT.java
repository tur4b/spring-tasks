package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.dao.TrainingTypeRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.request.TrainerUpdateRequest;
import org.example.dto.request.UpdateStatusRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("TrainerController Integration Tests")
class TrainerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    // ── public endpoint ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /trainers - returns 201 with credentials and X-Auth-Token header (public endpoint)")
    void registerTrainer_ReturnsCreatedWithTokenAndCredentials() throws Exception {
        TrainingType type = trainingTypeRepository.saveAndFlush(trainingType(TrainingTypeName.CARDIO));

        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TrainerCreateRequest("John", "Doe", type.getId()))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Auth-Token"))
                .andExpect(jsonPath("$.message").value("Trainer registered successfully. Please save your credentials."))
                .andExpect(jsonPath("$.data.username").value("john.doe"))
                .andExpect(jsonPath("$.data.password").isNotEmpty());
    }

    // ── unauthenticated cases ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /trainers/{username} - returns 403 when no JWT token is supplied")
    void getTrainerProfile_ReturnsForbiddenWithoutJwtToken() throws Exception {
        mockMvc.perform(get("/trainers/{username}", "john.doe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /trainers - returns 403 when no JWT token is supplied")
    void updateTrainer_ReturnsForbiddenWithoutJwtToken() throws Exception {
        mockMvc.perform(put("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TrainerUpdateRequest("john.doe", "Johnny", "Smith", 1))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /trainers/status - returns 403 when no JWT token is supplied")
    void statusUpdate_ReturnsForbiddenWithoutJwtToken() throws Exception {
        mockMvc.perform(patch("/trainers/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest("john.doe", false))))
                .andExpect(status().isForbidden());
    }

    // ── authenticated cases ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /trainers/{username} - returns trainer profile with valid JWT token")
    void getTrainerProfile_ReturnsPersistedDetailsWithJwtToken() throws Exception {
        TrainingType type = trainingTypeRepository.saveAndFlush(trainingType(TrainingTypeName.STRENGTH));
        String trainerResponse = registerTrainer("John", "Doe", type.getId());
        String username = JsonPath.read(trainerResponse, "$.data.username");
        String password = JsonPath.read(trainerResponse, "$.data.password");
        String jwtToken = obtainJwtToken(username, password);

        mockMvc.perform(get("/trainers/{username}", username)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Trainer profile view"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.specializationId").value(type.getId()));
    }

    @Test
    @DisplayName("PUT /trainers - updates profile fields with valid JWT token")
    void updateTrainer_UpdatesProfileFieldsWithJwtToken() throws Exception {
        TrainingType initialType = trainingTypeRepository.saveAndFlush(trainingType(TrainingTypeName.CARDIO));
        TrainingType newType = trainingTypeRepository.saveAndFlush(trainingType(TrainingTypeName.STRENGTH));

        String trainerResponse = registerTrainer("John", "Doe", initialType.getId());
        String username = JsonPath.read(trainerResponse, "$.data.username");
        String password = JsonPath.read(trainerResponse, "$.data.password");
        String jwtToken = obtainJwtToken(username, password);

        mockMvc.perform(put("/trainers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TrainerUpdateRequest(username, "Johnny", "Smith", newType.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Trainer profile updated successfully"))
                .andExpect(jsonPath("$.data.specializationId").value(newType.getId()));
    }

    @Test
    @DisplayName("PATCH /trainers/status - deactivates trainer; profile still readable with isActive=false")
    void statusUpdate_DeactivatesTrainerWithJwtToken() throws Exception {
        TrainingType type = trainingTypeRepository.saveAndFlush(trainingType(TrainingTypeName.CARDIO));
        String trainerResponse = registerTrainer("John", "Doe", type.getId());
        String username = JsonPath.read(trainerResponse, "$.data.username");
        String password = JsonPath.read(trainerResponse, "$.data.password");
        String jwtToken = obtainJwtToken(username, password);

        mockMvc.perform(patch("/trainers/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest(username, false))))
                .andExpect(status().isOk());

        // trainer profile is still accessible; active flag changes but JWT auth is stateless
        mockMvc.perform(get("/trainers/{username}", username)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false));
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
