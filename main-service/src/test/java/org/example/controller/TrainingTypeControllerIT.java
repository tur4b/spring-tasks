package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.dao.TrainingTypeRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.TraineeCreateRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("TrainingTypeController Integration Tests")
class TrainingTypeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    // ── unauthenticated cases ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /training-types - returns 403 when no JWT token is supplied")
    void getAllTrainingTypes_ReturnsForbiddenWithoutJwtToken() throws Exception {
        mockMvc.perform(get("/training-types"))
                .andExpect(status().isForbidden());
    }

    // ── authenticated cases ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /training-types - returns all training types with valid JWT token")
    void getAllTrainingTypes_ReturnsPersistedTypesWithJwtToken() throws Exception {
        TrainingType cardio = new TrainingType();
        cardio.setName(TrainingTypeName.CARDIO);
        trainingTypeRepository.saveAndFlush(cardio);

        TrainingType strength = new TrainingType();
        strength.setName(TrainingTypeName.STRENGTH);
        trainingTypeRepository.saveAndFlush(strength);

        String registerResponse = mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeCreateRequest("Alice", "Smith", "Main street", LocalDate.of(1995, 1, 10)))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String username = JsonPath.read(registerResponse, "$.data.username");
        String password = JsonPath.read(registerResponse, "$.data.password");
        String jwtToken = obtainJwtToken(username, password);

        mockMvc.perform(get("/training-types")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("TrainingType list"))
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[1].name").exists());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private String obtainJwtToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.data.accessToken");
    }
}
