package org.example.service;

import org.example.dao.TrainingDAO;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.request.TrainingUpdateRequest;
import org.example.dto.response.TrainingDTO;
import org.example.entity.Training;
import org.example.entity.TrainingType;
import org.example.mapper.TrainingMapper;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
import org.example.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingServiceImpl Unit Tests")
class TrainingServiceImplTest {

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private TrainingDAO trainingDAO;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training training;
    private TrainingDTO trainingDTO;

    @BeforeEach
    void setUp() {
        training = new Training();
        training.setId(1L);
        training.setTraineeId(2L);
        training.setTrainerId(3L);
        training.setName("Morning Cardio");
        training.setType(TrainingType.CARDIO);
        training.setDate(LocalDate.of(2026, 5, 7));
        training.setDuration(60);

        trainingDTO = new TrainingDTO(1L, 2L, 3L, "Morning Cardio", TrainingType.CARDIO,
                LocalDate.of(2026, 5, 7), 60, LocalDateTime.now());
    }

    @Test
    @DisplayName("findAll - returns list of TrainingDTOs")
    void findAll_ReturnsMappedDTOs() {
        when(trainingDAO.findAll()).thenReturn(List.of(training));
        when(trainingMapper.toDTO(training)).thenReturn(trainingDTO);

        List<TrainingDTO> result = trainingService.findAll();

        assertThat(result).hasSize(1).containsExactly(trainingDTO);
        verify(trainingDAO).findAll();
    }

    @Test
    @DisplayName("findAll - returns empty list when no trainings exist")
    void findAll_EmptyList() {
        when(trainingDAO.findAll()).thenReturn(List.of());

        assertThat(trainingService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findTrainingById - returns TrainingDTO when found")
    void findTrainingById_Found_ReturnsDTO() {
        when(trainingDAO.findById(1L)).thenReturn(Optional.of(training));
        when(trainingMapper.toDTO(training)).thenReturn(trainingDTO);

        TrainingDTO result = trainingService.findTrainingById(1L);

        assertThat(result).isEqualTo(trainingDTO);
    }

    @Test
    @DisplayName("findTrainingById - throws RuntimeException when not found")
    void findTrainingById_NotFound_ThrowsException() {
        when(trainingDAO.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.findTrainingById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Training not found with ID: 99");
    }

    @Test
    @DisplayName("createTraining - creates training and returns TrainingDTO")
    void createTraining_ValidRequest_ReturnsDTO() {
        TrainingCreateRequest request = new TrainingCreateRequest(2L, 3L, "Morning Cardio",
                TrainingType.CARDIO, LocalDate.of(2026, 5, 7), 60);

        when(traineeService.existsById(2L)).thenReturn(true);
        when(trainerService.existsById(3L)).thenReturn(true);
        when(trainingMapper.toEntity(request)).thenReturn(training);
        when(trainingDAO.create(training)).thenReturn(training);
        when(trainingMapper.toDTO(training)).thenReturn(trainingDTO);

        TrainingDTO result = trainingService.createTraining(request);

        assertThat(result).isEqualTo(trainingDTO);
        verify(trainingDAO).create(training);
    }

    @Test
    @DisplayName("createTraining - throws IllegalArgumentException for null request")
    void createTraining_NullRequest_ThrowsException() {
        assertThatThrownBy(() -> trainingService.createTraining(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TrainingCreateRequest cannot be null");
    }

    @Test
    @DisplayName("createTraining - throws RuntimeException when trainee does not exist")
    void createTraining_TraineeNotFound_ThrowsException() {
        TrainingCreateRequest request = new TrainingCreateRequest(99L, 3L, "Cardio",
                TrainingType.CARDIO, LocalDate.now(), 30);

        when(traineeService.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TraineeId not found");
    }

    @Test
    @DisplayName("createTraining - throws RuntimeException when trainer does not exist")
    void createTraining_TrainerNotFound_ThrowsException() {
        TrainingCreateRequest request = new TrainingCreateRequest(2L, 99L, "Cardio",
                TrainingType.CARDIO, LocalDate.now(), 30);

        when(traineeService.existsById(2L)).thenReturn(true);
        when(trainerService.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("TrainerId not found");
    }

    @Test
    @DisplayName("updateTraining - updates and returns TrainingDTO")
    void updateTraining_ValidRequest_ReturnsUpdatedDTO() {
        TrainingUpdateRequest request = new TrainingUpdateRequest(2L, 3L, "Evening Strength",
                TrainingType.STRENGTH, LocalDate.of(2026, 6, 1), 90);

        when(traineeService.existsById(2L)).thenReturn(true);
        when(trainerService.existsById(3L)).thenReturn(true);
        when(trainingDAO.findById(1L)).thenReturn(Optional.of(training));
        when(trainingMapper.toDTO(training)).thenReturn(trainingDTO);

        TrainingDTO result = trainingService.updateTraining(1L, request);

        assertThat(result).isEqualTo(trainingDTO);
        verify(trainingDAO).update(training);
        assertThat(training.getName()).isEqualTo("Evening Strength");
        assertThat(training.getDuration()).isEqualTo(90);
    }

    @Test
    @DisplayName("updateTraining - throws IllegalArgumentException for null trainingId")
    void updateTraining_NullId_ThrowsException() {
        assertThatThrownBy(() -> trainingService.updateTraining(null,
                new TrainingUpdateRequest(2L, 3L, "name", TrainingType.CARDIO, LocalDate.now(), 30)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updateTraining - throws IllegalArgumentException for null request")
    void updateTraining_NullRequest_ThrowsException() {
        assertThatThrownBy(() -> trainingService.updateTraining(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updateTraining - throws RuntimeException when training not found")
    void updateTraining_NotFound_ThrowsException() {
        TrainingUpdateRequest request = new TrainingUpdateRequest(2L, 3L, "name",
                TrainingType.CARDIO, LocalDate.now(), 30);

        when(traineeService.existsById(2L)).thenReturn(true);
        when(trainerService.existsById(3L)).thenReturn(true);
        when(trainingDAO.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.updateTraining(99L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Training not found with ID: 99");
    }

    @Test
    @DisplayName("deleteTraining - returns true when deletion succeeds")
    void deleteTraining_ReturnsTrue() {
        when(trainingDAO.deleteById(1L)).thenReturn(true);

        assertThat(trainingService.deleteTraining(1L)).isTrue();
        verify(trainingDAO).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTraining - returns false when training not found")
    void deleteTraining_ReturnsFalse() {
        when(trainingDAO.deleteById(99L)).thenReturn(false);

        assertThat(trainingService.deleteTraining(99L)).isFalse();
    }
}

