package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TraineeCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("AuthController Integration Tests")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── public endpoint ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login - returns JWT token for valid credentials (public endpoint)")
    void login_ReturnsJwtTokenForValidCredentials() throws Exception {
        String registerResponse = registerTrainee("Alice", "Smith");
        String username = JsonPath.read(registerResponse, "$.data.username");
        String password = JsonPath.read(registerResponse, "$.data.password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest(username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    // ── unauthenticated cases ──────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /auth/change-password - returns 403 when no JWT token is supplied")
    void changePassword_ReturnsForbiddenWithoutJwtToken() throws Exception {
        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("some.user", "old-pass", "new-pass"))))
                .andExpect(status().isForbidden());
    }

    // ── authenticated cases ────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /auth/change-password - updates password when authenticated via JWT")
    void changePassword_UpdatesPasswordWithJwtAuthentication() throws Exception {
        String registerResponse = registerTrainee("Alice", "Smith");
        String username = JsonPath.read(registerResponse, "$.data.username");
        String password = JsonPath.read(registerResponse, "$.data.password");

        String jwtToken = obtainJwtToken(username, password);

        mockMvc.perform(put("/auth/change-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest(username, password, "new-secret"))))
                .andExpect(status().isOk());

        assertThat(passwordEncoder.matches("new-secret",
                userRepository.findByUsername(username).orElseThrow().getPassword())).isTrue();
    }

    // ── helpers ────────────────────────────────────────────────────────────────

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
}





