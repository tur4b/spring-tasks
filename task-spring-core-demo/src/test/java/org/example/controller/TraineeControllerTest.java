package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.exception.handler.GlobalExceptionHandler;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerTraineeRelationService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeController Unit Tests")
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerTraineeRelationService trainerTraineeRelationService;

    @InjectMocks
    private TraineeController traineeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(traineeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    // ─── POST /trainees ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /trainees - 201 CREATED with generated credentials")
    void registerTrainee_ValidPayload_Returns201() throws Exception {
        UserCredentialsDTO creds = new UserCredentialsDTO(null, "john.doe", "gen-pw");
        when(traineeService.createTrainee(any())).thenReturn(creds);

        String body = objectMapper.writeValueAsString(
                new TraineeCreateRequest("John", "Doe", "Baku", LocalDate.of(2000, 1, 1)));

        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("john.doe"));
    }

    @Test
    @DisplayName("POST /trainees - 400 BAD REQUEST when first name is blank")
    void registerTrainee_BlankFirstName_Returns400() throws Exception {
        mockMvc.perform(post("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"Doe\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    // ─── GET /trainees/{username} ────────────────────────────────────────────

    @Test
    @DisplayName("GET /trainees/{username} - 200 OK with profile view")
    void getTraineeProfile_ExistingUsername_Returns200() throws Exception {
        TraineeProfileView view = new TraineeProfileView("John", "Doe", "Baku",
                LocalDate.of(2000, 1, 1), true, List.of());
        when(traineeService.findTraineeViewByUsername("john.doe")).thenReturn(view);

        mockMvc.perform(get("/trainees/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }

    @Test
    @DisplayName("GET /trainees/{username} - 404 NOT FOUND when trainee missing")
    void getTraineeProfile_NotFound_Returns404() throws Exception {
        when(traineeService.findTraineeViewByUsername("ghost"))
                .thenThrow(new NotFoundException("Trainee not found", ErrorResponse.ErrorPointer.username));

        mockMvc.perform(get("/trainees/ghost"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /trainees ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /trainees - 200 OK with updated profile")
    void updateTrainee_ValidPayload_Returns200() throws Exception {
        TraineeProfileView updated = new TraineeProfileView("Jane", "Doe", "London",
                LocalDate.of(1999, 5, 5), true, List.of());
        when(traineeService.updateTrainee(any())).thenReturn(updated);

        String body = objectMapper.writeValueAsString(
                new TraineeUpdateRequest("jane.doe", "Jane", "Doe", "London", LocalDate.of(1999, 5, 5)));

        mockMvc.perform(put("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test
    @DisplayName("PUT /trainees - 400 when username is blank")
    void updateTrainee_BlankUsername_Returns400() throws Exception {
        String body = "{\"username\":\"\",\"firstName\":\"Jane\",\"lastName\":\"Doe\"}";

        mockMvc.perform(put("/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    // ─── DELETE /trainees/{username} ─────────────────────────────────────────

    @Test
    @DisplayName("DELETE /trainees/{username} - 200 OK when deleted")
    void deleteTrainee_ExistingUsername_Returns200() throws Exception {
        when(traineeService.deleteTrainee("john.doe")).thenReturn(true);

        mockMvc.perform(delete("/trainees/john.doe"))
                .andExpect(status().isOk());

        verify(traineeService).deleteTrainee("john.doe");
    }

    // ─── PUT /trainees/{username}/trainers ───────────────────────────────────

    @Test
    @DisplayName("PUT /trainees/{username}/trainers - 200 OK with updated trainers list")
    void updateTraineeTrainersList_ValidPayload_Returns200() throws Exception {
        TraineeProfileTrainerDTO trainerDto = new TraineeProfileTrainerDTO("trainer.one", "Tr", "One", 1);
        when(trainerTraineeRelationService.updateTraineeTrainers(any())).thenReturn(List.of(trainerDto));

        String body = objectMapper.writeValueAsString(new TraineeUpdateTrainersRequest(
                "john.doe",
                List.of(new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer.one"))));

        mockMvc.perform(put("/trainees/john.doe/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("trainer.one"));
    }

    @Test
    @DisplayName("PUT /trainees/{username}/trainers - 400 when trainers list is empty")
    void updateTraineeTrainersList_EmptyList_Returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                new TraineeUpdateTrainersRequest("john.doe", List.of()));

        mockMvc.perform(put("/trainees/john.doe/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerTraineeRelationService);
    }

    // ─── GET /trainees/trainings ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /trainees/trainings - 200 OK with trainings list")
    void getTraineeTrainingsList_ValidCriteria_Returns200() throws Exception {
        when(traineeService.findTrainingsOfTraineeByCriteria(any())).thenReturn(List.of());

        mockMvc.perform(get("/trainees/trainings")
                        .param("traineeUsername", "john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ─── PATCH /trainees/status ──────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /trainees/status - 200 OK when status updated")
    void statusUpdate_ValidPayload_Returns200() throws Exception {
        doNothing().when(traineeService).updateStatus(any());

        mockMvc.perform(patch("/trainees/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john.doe\",\"active\":true}"))
                .andExpect(status().isOk());

        verify(traineeService).updateStatus(any());
    }

    @Test
    @DisplayName("PATCH /trainees/status - 400 when username is blank")
    void statusUpdate_BlankUsername_Returns400() throws Exception {
        mockMvc.perform(patch("/trainees/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"active\":true}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }
}

