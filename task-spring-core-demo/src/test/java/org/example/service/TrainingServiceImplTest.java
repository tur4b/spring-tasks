package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.dao.TrainingRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.TrainingCreateRequest;
import org.example.mapper.TrainingMapper;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
import org.example.service.api.TrainingTypeService;
import org.example.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingServiceImpl Unit Tests")
class TrainingServiceImplTest {

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Test
    @DisplayName("createTraining - throws when trainer-trainee relation does not exist")
    void createTraining_NoRelation_Throws() {
        TrainingCreateRequest request = new TrainingCreateRequest(1L, 2L, "Cardio", 1, LocalDate.now().plusDays(1), 30);
        AuthRequest auth = new AuthRequest("u", "p");

        when(trainingTypeService.existsById(1)).thenReturn(true);
        when(traineeService.existsById(1L)).thenReturn(true);
        when(trainerService.existsById(2L)).thenReturn(true);
        when(trainerService.existsTrainerTraineeRelation(2L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> trainingService.createTraining(request, auth))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("relation does not exists");
    }

    @Test
    @DisplayName("deleteTraining - returns true when repository updates one row")
    void deleteTraining_ReturnsTrue_WhenSoftDeleteAffectsRow() {
        when(trainingRepository.softDeleteById(10L)).thenReturn(1);

        boolean result = trainingService.deleteTraining(10L, new AuthRequest("u", "p"));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("createTraining - throws EntityNotFoundException when training type id does not exist")
    void createTraining_ShouldThrowEntityNotFoundException_WhenTrainingTypeIdDoesNotExist() {
        TrainingCreateRequest request = new TrainingCreateRequest(1L, 2L, "Cardio", 404, LocalDate.now().plusDays(1), 30);

        when(trainingTypeService.existsById(404)).thenReturn(false);

        assertThatThrownBy(() -> trainingService.createTraining(request, new AuthRequest("u", "p")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TraineeType not found");

        verify(trainingRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTraining - returns false when repository soft-delete affects zero rows")
    void deleteTraining_ShouldReturnFalse_WhenSoftDeleteAffectsNoRows() {
        when(trainingRepository.softDeleteById(55L)).thenReturn(0);

        boolean result = trainingService.deleteTraining(55L, new AuthRequest("u", "p"));

        assertThat(result).isFalse();
    }
}

