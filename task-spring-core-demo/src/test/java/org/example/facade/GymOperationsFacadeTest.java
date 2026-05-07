package org.example.facade;

import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.entity.TrainingType;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
import org.example.service.api.TrainingService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GymOperationsFacade Unit Tests")
class GymOperationsFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private GymOperationsFacade facade;

    private TraineeDTO traineeDTO;
    private TrainerDTO trainerDTO;
    private TrainingDTO trainingDTO;

    @BeforeEach
    void setUp() {
        traineeDTO  = new TraineeDTO(1L, 10L, "123 Main St", LocalDate.of(1995, 5, 10), LocalDateTime.now());
        trainerDTO  = new TrainerDTO(2L, 20L, TrainingType.CARDIO, LocalDateTime.now());
        trainingDTO = new TrainingDTO(3L, 1L, 2L, "Morning Cardio", TrainingType.CARDIO,
                LocalDate.of(2026, 5, 7), 60, LocalDateTime.now());
    }

    @Test
    @DisplayName("findAllTrainees - delegates to TraineeService.findAll()")
    void findAllTrainees_DelegatesToService() {
        when(traineeService.findAll()).thenReturn(List.of(traineeDTO));

        List<TraineeDTO> result = facade.findAllTrainees();

        assertThat(result).containsExactly(traineeDTO);
        verify(traineeService).findAll();
    }

    @Test
    @DisplayName("findTraineeById - delegates to TraineeService.findTraineeById()")
    void findTraineeById_DelegatesToService() {
        when(traineeService.findTraineeById(1L)).thenReturn(traineeDTO);

        TraineeDTO result = facade.findTraineeById(1L);

        assertThat(result).isEqualTo(traineeDTO);
        verify(traineeService).findTraineeById(1L);
    }

    @Test
    @DisplayName("createTrainee - delegates to TraineeService.createTrainee()")
    void createTrainee_DelegatesToService() {
        TraineeCreateRequest request = new TraineeCreateRequest("Doe", "John", "123 Main St", LocalDate.of(1995, 5, 10));
        when(traineeService.createTrainee(request)).thenReturn(traineeDTO);

        TraineeDTO result = facade.createTrainee(request);

        assertThat(result).isEqualTo(traineeDTO);
        verify(traineeService).createTrainee(request);
    }

    @Test
    @DisplayName("updateTrainee - delegates to TraineeService.updateTrainee()")
    void updateTrainee_DelegatesToService() {
        TraineeUpdateRequest request = new TraineeUpdateRequest("Doe", "John", "456 St", LocalDate.now());
        when(traineeService.updateTrainee(1L, request)).thenReturn(traineeDTO);

        TraineeDTO result = facade.updateTrainee(1L, request);

        assertThat(result).isEqualTo(traineeDTO);
        verify(traineeService).updateTrainee(1L, request);
    }

    @Test
    @DisplayName("deleteTrainee - delegates to TraineeService.deleteTrainee()")
    void deleteTrainee_DelegatesToService() {
        when(traineeService.deleteTrainee(1L)).thenReturn(true);

        boolean result = facade.deleteTrainee(1L);

        assertThat(result).isTrue();
        verify(traineeService).deleteTrainee(1L);
    }

    @Test
    @DisplayName("findAllTrainers - delegates to TrainerService.findAll()")
    void findAllTrainers_DelegatesToService() {
        when(trainerService.findAll()).thenReturn(List.of(trainerDTO));

        List<TrainerDTO> result = facade.findAllTrainers();

        assertThat(result).containsExactly(trainerDTO);
        verify(trainerService).findAll();
    }

    @Test
    @DisplayName("findTrainerById - delegates to TrainerService.findTrainerById()")
    void findTrainerById_DelegatesToService() {
        when(trainerService.findTrainerById(2L)).thenReturn(trainerDTO);

        TrainerDTO result = facade.findTrainerById(2L);

        assertThat(result).isEqualTo(trainerDTO);
        verify(trainerService).findTrainerById(2L);
    }

    @Test
    @DisplayName("createTrainer - delegates to TrainerService.createTrainer()")
    void createTrainer_DelegatesToService() {
        TrainerCreateRequest request = new TrainerCreateRequest("Smith", "Jane", TrainingType.CARDIO);
        when(trainerService.createTrainer(request)).thenReturn(trainerDTO);

        TrainerDTO result = facade.createTrainer(request);

        assertThat(result).isEqualTo(trainerDTO);
        verify(trainerService).createTrainer(request);
    }

    @Test
    @DisplayName("updateTrainer - delegates to TrainerService.updateTrainer()")
    void updateTrainer_DelegatesToService() {
        TrainerUpdateRequest request = new TrainerUpdateRequest("Smith", "Jane", TrainingType.STRENGTH);
        when(trainerService.updateTrainer(2L, request)).thenReturn(trainerDTO);

        TrainerDTO result = facade.updateTrainer(2L, request);

        assertThat(result).isEqualTo(trainerDTO);
        verify(trainerService).updateTrainer(2L, request);
    }

    @Test
    @DisplayName("deleteTrainer - delegates to TrainerService.deleteTrainer()")
    void deleteTrainer_DelegatesToService() {
        when(trainerService.deleteTrainer(2L)).thenReturn(true);

        boolean result = facade.deleteTrainer(2L);

        assertThat(result).isTrue();
        verify(trainerService).deleteTrainer(2L);
    }

    @Test
    @DisplayName("findAllTrainings - delegates to TrainingService.findAll()")
    void findAllTrainings_DelegatesToService() {
        when(trainingService.findAll()).thenReturn(List.of(trainingDTO));

        List<TrainingDTO> result = facade.findAllTrainings();

        assertThat(result).containsExactly(trainingDTO);
        verify(trainingService).findAll();
    }

    @Test
    @DisplayName("findTrainingById - delegates to TrainingService.findTrainingById()")
    void findTrainingById_DelegatesToService() {
        when(trainingService.findTrainingById(3L)).thenReturn(trainingDTO);

        TrainingDTO result = facade.findTrainingById(3L);

        assertThat(result).isEqualTo(trainingDTO);
        verify(trainingService).findTrainingById(3L);
    }

    @Test
    @DisplayName("createTraining - delegates to TrainingService.createTraining()")
    void createTraining_DelegatesToService() {
        TrainingCreateRequest request = new TrainingCreateRequest(1L, 2L, "Morning Cardio",
                TrainingType.CARDIO, LocalDate.of(2026, 5, 7), 60);
        when(trainingService.createTraining(request)).thenReturn(trainingDTO);

        TrainingDTO result = facade.createTraining(request);

        assertThat(result).isEqualTo(trainingDTO);
        verify(trainingService).createTraining(request);
    }

    @Test
    @DisplayName("updateTraining - delegates to TrainingService.updateTraining()")
    void updateTraining_DelegatesToService() {
        TrainingUpdateRequest request = new TrainingUpdateRequest(1L, 2L, "Evening Strength",
                TrainingType.STRENGTH, LocalDate.of(2026, 6, 1), 90);
        when(trainingService.updateTraining(3L, request)).thenReturn(trainingDTO);

        TrainingDTO result = facade.updateTraining(3L, request);

        assertThat(result).isEqualTo(trainingDTO);
        verify(trainingService).updateTraining(3L, request);
    }

    @Test
    @DisplayName("deleteTraining - delegates to TrainingService.deleteTraining()")
    void deleteTraining_DelegatesToService() {
        when(trainingService.deleteTraining(3L)).thenReturn(true);

        boolean result = facade.deleteTraining(3L);

        assertThat(result).isTrue();
        verify(trainingService).deleteTraining(3L);
    }
}

