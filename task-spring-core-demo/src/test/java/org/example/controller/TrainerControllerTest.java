package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.exception.handler.GlobalExceptionHandler;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerController Unit Tests")
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainerController trainerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(trainerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    // ─── POST /trainers ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /trainers - 201 CREATED with generated credentials")
    void registerTrainer_ValidPayload_Returns201() throws Exception {
        UserCredentialsDTO creds = new UserCredentialsDTO(null, "jane.coach", "gen-pw");
        when(trainerService.createTrainer(any())).thenReturn(creds);

        String body = objectMapper.writeValueAsString(new TrainerCreateRequest("Jane", "Coach", 1));

        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("jane.coach"));
    }

    @Test
    @DisplayName("POST /trainers - 400 when first name is blank")
    void registerTrainer_BlankFirstName_Returns400() throws Exception {
        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"Coach\",\"specializationId\":1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    @DisplayName("POST /trainers - 400 when specializationId is null")
    void registerTrainer_NullSpecialization_Returns400() throws Exception {
        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\",\"lastName\":\"Coach\",\"specializationId\":null}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    // ─── GET /trainers/{username} ────────────────────────────────────────────

    @Test
    @DisplayName("GET /trainers/{username} - 200 OK with profile view")
    void getTrainerProfile_ExistingUsername_Returns200() throws Exception {
        TrainerProfileView view = new TrainerProfileView("Jane", "Coach", 1, true, List.of());
        when(trainerService.findTrainerViewByUsername("jane.coach")).thenReturn(view);

        mockMvc.perform(get("/trainers/jane.coach"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test
    @DisplayName("GET /trainers/{username} - 404 when trainer not found")
    void getTrainerProfile_NotFound_Returns404() throws Exception {
        when(trainerService.findTrainerViewByUsername("ghost"))
                .thenThrow(new NotFoundException("Trainer not found", ErrorResponse.ErrorPointer.username));

        mockMvc.perform(get("/trainers/ghost"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /trainers ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /trainers - 200 OK with updated profile")
    void updateTrainer_ValidPayload_Returns200() throws Exception {
        TrainerProfileView updated = new TrainerProfileView("Jane", "Smith", 2, true, List.of());
        when(trainerService.updateTrainer(any())).thenReturn(updated);

        String body = objectMapper.writeValueAsString(
                new TrainerUpdateRequest("jane.coach", "Jane", "Smith", 2));

        mockMvc.perform(put("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastName").value("Smith"));
    }

    @Test
    @DisplayName("PUT /trainers - 400 when username is blank")
    void updateTrainer_BlankUsername_Returns400() throws Exception {
        mockMvc.perform(put("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"firstName\":\"Jane\",\"lastName\":\"Coach\",\"specializationId\":1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    // ─── DELETE /trainers/{username} ─────────────────────────────────────────

    @Test
    @DisplayName("DELETE /trainers/{username} - 200 OK when deleted")
    void deleteTrainer_ExistingUsername_Returns200() throws Exception {
        when(trainerService.deleteTrainer("jane.coach")).thenReturn(true);

        mockMvc.perform(delete("/trainers/jane.coach"))
                .andExpect(status().isOk());

        verify(trainerService).deleteTrainer("jane.coach");
    }

    // ─── GET /trainers/not-assigned-on-trainee/{traineeUsername} ─────────────

    @Test
    @DisplayName("GET /trainers/not-assigned-on-trainee/{username} - 200 OK with unassigned trainers")
    void notAssignedOnTraineeTrainers_ValidUsername_Returns200() throws Exception {
        TraineeProfileTrainerDTO dto = new TraineeProfileTrainerDTO("free.trainer", "Free", "Trainer", 1);
        when(trainerService.findTrainersNotAssignedToTrainee("john.doe")).thenReturn(List.of(dto));

        mockMvc.perform(get("/trainers/not-assigned-on-trainee/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("free.trainer"));
    }

    // ─── GET /trainers/trainings ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /trainers/trainings - 200 OK with trainings list")
    void getTrainerTrainingsList_ValidCriteria_Returns200() throws Exception {
        when(trainerService.findTrainingsOfTrainerByCriteria(any())).thenReturn(List.of());

        mockMvc.perform(get("/trainers/trainings")
                        .param("trainerUsername", "jane.coach"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ─── PATCH /trainers/status ──────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /trainers/status - 200 OK when status updated")
    void statusUpdate_ValidPayload_Returns200() throws Exception {
        doNothing().when(trainerService).updateStatus(any());

        mockMvc.perform(patch("/trainers/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"jane.coach\",\"active\":false}"))
                .andExpect(status().isOk());

        verify(trainerService).updateStatus(any());
    }

    @Test
    @DisplayName("PATCH /trainers/status - 400 when active flag is null")
    void statusUpdate_NullActive_Returns400() throws Exception {
        mockMvc.perform(patch("/trainers/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"jane.coach\",\"active\":null}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }
}

