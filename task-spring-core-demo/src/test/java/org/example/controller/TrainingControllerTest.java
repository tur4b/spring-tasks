package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.TrainingDTO;
import org.example.exception.handler.GlobalExceptionHandler;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.TrainingService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingController Unit Tests")
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController trainingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(trainingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    // ─── POST /trainings ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /trainings - 200 OK when training created successfully")
    void createTraining_ValidPayload_Returns200() throws Exception {
        TrainingDTO dto = new TrainingDTO(1L, 20L, 10L, "Cardio Morning", 1,
                LocalDate.now().plusDays(1), 60);
        when(trainingService.createTraining(any())).thenReturn(dto);

        String body = objectMapper.writeValueAsString(
                new TrainingCreateRequest("trainee.user", "trainer.user", "Cardio Morning",
                        LocalDate.now().plusDays(1), 60));

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(trainingService).createTraining(any());
    }

    @Test
    @DisplayName("POST /trainings - 400 when trainee username is blank")
    void createTraining_BlankTraineeUsername_Returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                new TrainingCreateRequest("", "trainer.user", "Cardio",
                        LocalDate.now().plusDays(1), 60));

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }

    @Test
    @DisplayName("POST /trainings - 400 when trainer username is blank")
    void createTraining_BlankTrainerUsername_Returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                new TrainingCreateRequest("trainee.user", "", "Cardio",
                        LocalDate.now().plusDays(1), 60));

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }

    @Test
    @DisplayName("POST /trainings - 400 when date is in the past")
    void createTraining_PastDate_Returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                new TrainingCreateRequest("trainee.user", "trainer.user", "Cardio",
                        LocalDate.now().minusDays(1), 60));

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }

    @Test
    @DisplayName("POST /trainings - 400 when duration is not positive")
    void createTraining_NegativeDuration_Returns400() throws Exception {
        String body = "{\"traineeUsername\":\"trainee.user\",\"trainerUsername\":\"trainer.user\","
                + "\"name\":\"Cardio\",\"date\":\"" + LocalDate.now().plusDays(1) + "\",\"duration\":-5}";

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }

    @Test
    @DisplayName("POST /trainings - 404 when trainer-trainee relation missing")
    void createTraining_RelationMissing_Returns404() throws Exception {
        when(trainingService.createTraining(any()))
                .thenThrow(new NotFoundException("Trainer and Trainee relation does not exists",
                        ErrorResponse.ErrorPointer.id));

        String body = objectMapper.writeValueAsString(
                new TrainingCreateRequest("trainee.user", "trainer.user", "Cardio",
                        LocalDate.now().plusDays(1), 60));

        mockMvc.perform(post("/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}

