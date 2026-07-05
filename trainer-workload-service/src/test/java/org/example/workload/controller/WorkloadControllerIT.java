package org.example.workload.controller;

import org.example.common.security.JwtService;
import org.example.workload.store.WorkloadStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Autowired
    private WorkloadStore workloadStore;

    @BeforeEach
    void resetWorkloadStore() {
        workloadStore.clear();
    }

    @Test
    @DisplayName("POST /trainers/{username}/workloads requires JWT")
    void addWorkload_RequiresJwt() throws Exception {
        mockMvc.perform(post("/trainers/trainer.one/workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trainerFirstName": "John",
                                  "trainerLastName": "Smith",
                                  "isActive": true,
                                  "trainingDate": "2026-06-15",
                                  "trainingDuration": 60
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST and GET monthly summary succeed with JWT")
    void addAndQueryMonthlyWorkload() throws Exception {
        String token = jwtService.generateToken("main-service");

        mockMvc.perform(post("/trainers/trainer.one/workloads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trainerFirstName": "John",
                                  "trainerLastName": "Smith",
                                  "isActive": true,
                                  "trainingDate": "2026-06-15",
                                  "trainingDuration": 60
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/trainers/trainer.one/workloads/years/2026/months/6")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(6))
                .andExpect(jsonPath("$.trainingSummaryDuration").value(60));
    }

    @Test
    @DisplayName("GET trainer summary returns aggregated workload")
    void getTrainerSummary() throws Exception {
        String token = jwtService.generateToken("main-service");

        mockMvc.perform(post("/trainers/trainer.one/workloads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trainerFirstName": "John",
                                  "trainerLastName": "Smith",
                                  "isActive": true,
                                  "trainingDate": "2026-06-15",
                                  "trainingDuration": 60
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/trainers/trainer.one/workloads")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("trainer.one"))
                .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(60));
    }

    @Test
    @DisplayName("DELETE workload entry returns 204")
    void deleteWorkload() throws Exception {
        String token = jwtService.generateToken("main-service");

        mockMvc.perform(post("/trainers/trainer.one/workloads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trainerFirstName": "John",
                                  "trainerLastName": "Smith",
                                  "isActive": true,
                                  "trainingDate": "2026-06-15",
                                  "trainingDuration": 60
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/trainers/trainer.one/workloads")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "trainerFirstName": "John",
                                  "trainerLastName": "Smith",
                                  "isActive": true,
                                  "trainingDate": "2026-06-15",
                                  "trainingDuration": 20
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/trainers/trainer.one/workloads/years/2026/months/6")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingSummaryDuration").value(40));
    }

    @Test
    @DisplayName("GET monthly summary returns 404 when trainer has no workload")
    void getMonthlySummary_NotFound() throws Exception {
        String token = jwtService.generateToken("main-service");

        mockMvc.perform(get("/trainers/unknown.trainer/workloads/years/2026/months/6")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
