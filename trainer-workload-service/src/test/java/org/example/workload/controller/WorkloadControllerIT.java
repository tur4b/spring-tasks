package org.example.workload.controller;

import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.ActionType;
import org.example.common.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("WorkloadController Integration Tests")
class WorkloadControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("POST /workloads requires JWT")
    void submitWorkload_RequiresJwt() throws Exception {
        mockMvc.perform(post("/workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trainerUsername": "trainer.one",
                                  "trainerFirstName": "John",
                                  "trainerLastName": "Smith",
                                  "isActive": true,
                                  "trainingDate": "2026-06-15",
                                  "trainingDuration": 60,
                                  "actionType": "ADD"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /workloads and GET monthly summary succeed with JWT")
    void submitAndQueryWorkload() throws Exception {
        String token = jwtService.generateToken("main-service");

        mockMvc.perform(post("/workloads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trainerUsername": "trainer.one",
                                  "trainerFirstName": "John",
                                  "trainerLastName": "Smith",
                                  "isActive": true,
                                  "trainingDate": "2026-06-15",
                                  "trainingDuration": 60,
                                  "actionType": "ADD"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/trainers/trainer.one/workloads")
                        .header("Authorization", "Bearer " + token)
                        .param("year", "2026")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(6))
                .andExpect(jsonPath("$.trainingSummaryDuration").value(60));
    }

    @Test
    @DisplayName("GET monthly summary returns 404 when trainer has no workload")
    void getMonthlySummary_NotFound() throws Exception {
        String token = jwtService.generateToken("main-service");

        mockMvc.perform(get("/trainers/unknown.trainer/workloads")
                        .header("Authorization", "Bearer " + token)
                        .param("year", "2026")
                        .param("month", "6"))
                .andExpect(status().isNotFound());
    }
}
