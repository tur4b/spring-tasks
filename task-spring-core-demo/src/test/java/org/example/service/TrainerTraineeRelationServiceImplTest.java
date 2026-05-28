package org.example.service;

import org.example.dao.TraineeRepository;
import org.example.dao.TrainerRepository;
import org.example.dto.request.TraineeUpdateTrainersRequest;
import org.example.dto.response.TraineeProfileTrainerDTO;
import org.example.dto.response.TrainerProfileTraineeDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.service.impl.TrainerTraineeRelationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerTraineeRelationService Unit Tests")
class TrainerTraineeRelationServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @InjectMocks
    private TrainerTraineeRelationServiceImpl relationService;

    // ─── existsTrainerTraineeRelation ──────────────────────────────────────────

    @Test
    @DisplayName("existsTrainerTraineeRelation - delegates to repository")
    void existsTrainerTraineeRelation_DelegatesToRepository() {
        when(trainerRepository.existsTrainerTraineeRelation("trainer.user", "trainee.user"))
                .thenReturn(true);

        assertThat(relationService.existsTrainerTraineeRelation("trainer.user", "trainee.user"))
                .isTrue();

        verify(trainerRepository).existsTrainerTraineeRelation("trainer.user", "trainee.user");
    }

    @Test
    @DisplayName("existsTrainerTraineeRelation - returns false when no relation exists")
    void existsTrainerTraineeRelation_NoRelation_ReturnsFalse() {
        when(trainerRepository.existsTrainerTraineeRelation("trainer.user", "trainee.user"))
                .thenReturn(false);

        assertThat(relationService.existsTrainerTraineeRelation("trainer.user", "trainee.user"))
                .isFalse();
    }

    // ─── findTrainersOfTraineeByTraineeUsername ────────────────────────────────

    @Test
    @DisplayName("findTrainersOfTraineeByTraineeUsername - delegates to repository")
    void findTrainersOfTraineeByTraineeUsername_DelegatesToRepository() {
        TraineeProfileTrainerDTO trainer = new TraineeProfileTrainerDTO("trainer1", "First", "Last", 1);
        when(trainerRepository.findTrainersOfTraineeByTraineeUsername("trainee.user"))
                .thenReturn(List.of(trainer));

        List<TraineeProfileTrainerDTO> result = relationService
                .findTrainersOfTraineeByTraineeUsername("trainee.user");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().username()).isEqualTo("trainer1");
    }

    @Test
    @DisplayName("findTrainersOfTraineeByTraineeUsername - returns empty list when no trainers assigned")
    void findTrainersOfTraineeByTraineeUsername_NoTrainers_ReturnsEmptyList() {
        when(trainerRepository.findTrainersOfTraineeByTraineeUsername("trainee.user"))
                .thenReturn(List.of());

        List<TraineeProfileTrainerDTO> result = relationService
                .findTrainersOfTraineeByTraineeUsername("trainee.user");

        assertThat(result).isEmpty();
    }

    // ─── findTraineesOfTrainerByTrainerUsername ────────────────────────────────

    @Test
    @DisplayName("findTraineesOfTrainerByTrainerUsername - delegates to repository")
    void findTraineesOfTrainerByTrainerUsername_DelegatesToRepository() {
        TrainerProfileTraineeDTO trainee = new TrainerProfileTraineeDTO("trainee1", "First", "Last");
        when(trainerRepository.findTraineesOfTrainerByTrainerUsername("trainer.user"))
                .thenReturn(List.of(trainee));

        List<TrainerProfileTraineeDTO> result = relationService
                .findTraineesOfTrainerByTrainerUsername("trainer.user");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().username()).isEqualTo("trainee1");
    }

    @Test
    @DisplayName("findTraineesOfTrainerByTrainerUsername - returns empty list when no trainees assigned")
    void findTraineesOfTrainerByTrainerUsername_NoTrainees_ReturnsEmptyList() {
        when(trainerRepository.findTraineesOfTrainerByTrainerUsername("trainer.user"))
                .thenReturn(List.of());

        List<TrainerProfileTraineeDTO> result = relationService
                .findTraineesOfTrainerByTrainerUsername("trainer.user");

        assertThat(result).isEmpty();
    }

    // ─── updateTraineeTrainers ────────────────────────────────────────────────

    @Test
    @DisplayName("updateTraineeTrainers - throws NotFoundException when trainee not found")
    void updateTraineeTrainers_TraineeNotFound_ThrowsNotFoundException() {
        when(traineeRepository.findByUserUsername("ghost.trainee")).thenReturn(Optional.empty());

        TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(
                "ghost.trainee", List.of(new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer1")));

        assertThatThrownBy(() -> relationService.updateTraineeTrainers(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found");

        verify(traineeRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTraineeTrainers - throws NotFoundException when trainer not found")
    void updateTraineeTrainers_TrainerNotFound_ThrowsNotFoundException() {
        Trainee trainee = buildTrainee("trainee.user");
        when(traineeRepository.findByUserUsername("trainee.user")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllByUsernames(List.of("trainer1"))).thenReturn(List.of());

        TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(
                "trainee.user", List.of(new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer1")));

        assertThatThrownBy(() -> relationService.updateTraineeTrainers(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Some trainers not found");

        verify(traineeRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTraineeTrainers - updates trainee trainers successfully")
    void updateTraineeTrainers_Success_UpdatesRelations() {
        Trainee trainee = buildTrainee("trainee.user");
        Trainer trainer1 = buildTrainer("trainer1");
        Trainer trainer2 = buildTrainer("trainer2");

        when(traineeRepository.findByUserUsername("trainee.user")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllByUsernames(List.of("trainer1", "trainer2")))
                .thenReturn(List.of(trainer1, trainer2));
        when(trainerRepository.findAllByTraineesId(trainee.getId())).thenReturn(List.of());

        TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(
                "trainee.user", List.of(
                    new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer1"),
                    new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer2")
                ));

        List<TraineeProfileTrainerDTO> result = relationService.updateTraineeTrainers(request);

        assertThat(result).hasSize(2);
        verify(traineeRepository).save(trainee);
        verify(trainerRepository).saveAll(List.of(trainer1, trainer2));
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private Trainee buildTrainee(String username) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Trainee");
        user.setLastName("User");

        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(user);
        return trainee;
    }

    private Trainer buildTrainer(String username) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Trainer");
        user.setLastName("User");

        TrainingType specialization = new TrainingType();
        specialization.setId(1);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);
        return trainer;
    }
}

