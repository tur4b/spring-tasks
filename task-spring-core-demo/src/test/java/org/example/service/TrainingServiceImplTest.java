package org.example.service;

import org.example.dao.TrainingRepository;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.TrainingDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.exception.model.NotFoundException;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
import org.example.service.api.TrainerTraineeRelationService;
import org.example.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingServiceImpl Unit Tests")
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainerTraineeRelationService trainerTraineeRelationService;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Test
    @DisplayName("createTraining persists training when trainer-trainee relation exists")
    void createTraining_CreatesTraining() {
        Trainer trainer = new Trainer();
        trainer.setId(2L);
        TrainingType type = new TrainingType();
        type.setId(3);
        type.setName(TrainingTypeName.CARDIO);
        trainer.setSpecialization(type);

        Trainee trainee = new Trainee();
        trainee.setId(1L);

        when(trainerTraineeRelationService.existsTrainerTraineeRelation("trainer.one", "trainee.one")).thenReturn(true);
        when(trainerService.findTrainerByUsername("trainer.one")).thenReturn(trainer);
        when(traineeService.findTraineeByUsername("trainee.one")).thenReturn(trainee);
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            training.setId(10L);
            return training;
        });

        TrainingDTO result = trainingService.createTraining(new TrainingCreateRequest("trainee.one", "trainer.one", "Strength", LocalDate.of(2026, 1, 1), 45));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.traineeId()).isEqualTo(1L);
        assertThat(result.trainerId()).isEqualTo(2L);
        assertThat(result.typeId()).isEqualTo(3);
        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        assertThat(captor.getValue().getTrainer()).isEqualTo(trainer);
        assertThat(captor.getValue().getTrainee()).isEqualTo(trainee);
    }

    @Test
    @DisplayName("createTraining throws when trainer-trainee relation is missing")
    void createTraining_ThrowsWhenRelationMissing() {
        when(trainerTraineeRelationService.existsTrainerTraineeRelation("trainer.one", "trainee.one")).thenReturn(false);

        assertThatThrownBy(() -> trainingService.createTraining(new TrainingCreateRequest("trainee.one", "trainer.one", "Strength", LocalDate.of(2026, 1, 1), 45)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer and Trainee relation does not exists");
    }

    @Test
    @DisplayName("deleteTraining returns true when a row is affected")
    void deleteTraining_ReturnsTrueWhenAffected() {
        when(trainingRepository.softDeleteById(5L)).thenReturn(1);

        assertThat(trainingService.deleteTraining(5L)).isTrue();
        verify(trainingRepository).softDeleteById(5L);
    }
}

