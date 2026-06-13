package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TraineeUpdateRequest;
import org.example.dto.request.UpdateStatusRequest;
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
@DisplayName("TraineeController Integration Tests")
class TraineeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── public endpoint ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /trainees - returns 201 with credentials and X-Auth-Token header (public endpoint)")
    void registerTrainee_ReturnsCreatedWithTokenAndCredentials() throws Exception {
        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeCreateRequest("Alice", "Smith", "Main street", LocalDate.of(1995, 1, 10)))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Auth-Token"))
                .andExpect(jsonPath("$.message").value("Trainee registered successfully."))
                .andExpect(jsonPath("$.data.username").value("alice.smith"))
                .andExpect(jsonPath("$.data.password").isNotEmpty());
    }

    // ── unauthenticated cases ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /trainees/{username} - returns 403 when no JWT token is supplied")
    void getTraineeProfile_ReturnsForbiddenWithoutJwtToken() throws Exception {
        mockMvc.perform(get("/trainees/{username}", "alice.smith"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /trainees - returns 403 when no JWT token is supplied")
    void updateTrainee_ReturnsForbiddenWithoutJwtToken() throws Exception {
        mockMvc.perform(put("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeUpdateRequest("alice.smith", "Alicia", "Johnson", "New address", LocalDate.of(1995, 1, 10)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /trainees/status - returns 403 when no JWT token is supplied")
    void statusUpdate_ReturnsForbiddenWithoutJwtToken() throws Exception {
        mockMvc.perform(patch("/trainees/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest("alice.smith", false))))
                .andExpect(status().isForbidden());
    }

    // ── authenticated cases ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /trainees/{username} - returns trainee profile with valid JWT token")
    void getTraineeProfile_ReturnsPersistedDetailsWithJwtToken() throws Exception {
        String registerResponse = registerTrainee("Alice", "Smith", "Main street");
        String username = JsonPath.read(registerResponse, "$.data.username");
        String password = JsonPath.read(registerResponse, "$.data.password");
        String jwtToken = obtainJwtToken(username, password);

        mockMvc.perform(get("/trainees/{username}", username)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Trainee profile view"))
                .andExpect(jsonPath("$.data.firstName").value("Alice"))
                .andExpect(jsonPath("$.data.lastName").value("Smith"));
    }

    @Test
    @DisplayName("PUT /trainees - updates profile fields with valid JWT token")
    void updateTrainee_UpdatesProfileFieldsWithJwtToken() throws Exception {
        String registerResponse = registerTrainee("Alice", "Smith", "Old address");
        String username = JsonPath.read(registerResponse, "$.data.username");
        String password = JsonPath.read(registerResponse, "$.data.password");
        String jwtToken = obtainJwtToken(username, password);

        mockMvc.perform(put("/trainees")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeUpdateRequest(username, "Alicia", "Johnson", "New address", LocalDate.of(1995, 1, 10)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Trainee profile updated successfully"))
                .andExpect(jsonPath("$.data.address").value("New address"));
    }

    @Test
    @DisplayName("PATCH /trainees/status - deactivates trainee; subsequent GET returns 404 with valid JWT token")
    void statusUpdate_DeactivatesTraineeWithJwtToken() throws Exception {
        String registerResponse = registerTrainee("Alice", "Smith", "Main street");
        String username = JsonPath.read(registerResponse, "$.data.username");
        String password = JsonPath.read(registerResponse, "$.data.password");
        String jwtToken = obtainJwtToken(username, password);

        mockMvc.perform(patch("/trainees/status")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest(username, false))))
                .andExpect(status().isOk());

        // profile query filters by active = true, so deactivated trainee returns 404
        mockMvc.perform(get("/trainees/{username}", username)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private String registerTrainee(String firstName, String lastName, String address) throws Exception {
        return mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TraineeCreateRequest(firstName, lastName, address, LocalDate.of(1995, 1, 10)))))
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
}
