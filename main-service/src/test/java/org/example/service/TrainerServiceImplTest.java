package org.example.service;

import org.example.dao.TraineeRepository;
import org.example.dao.TrainerRepository;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.request.TrainerUpdateRequest;
import org.example.dto.request.UpdateStatusRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.TrainerProfileView;
import org.example.dto.response.UserCredentialsDTO;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.service.api.TrainerTraineeRelationService;
import org.example.service.api.TrainingTypeService;
import org.example.service.api.UserService;
import org.example.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerServiceImpl Unit Tests")
class TrainerServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserService userService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TrainerTraineeRelationService trainerTraineeRelationService;

    @Mock
    private TraineeRepository traineeRepository;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Test
    @DisplayName("createTrainer persists trainer with generated credentials and specialization")
    void createTrainer_CreatesTrainer() {
        UserCredentialsDTO credentials = new UserCredentialsDTO(1L, "john.doe", "raw-password");
        User user = new User();
        user.setId(1L);
        user.setUsername("john.doe");
        TrainingType type = new TrainingType();
        type.setId(2);
        type.setName(TrainingTypeName.STRENGTH);

        when(userService.createUser(any())).thenReturn(credentials);
        when(userService.getReferenceById(1L)).thenReturn(user);
        when(trainingTypeService.existsById(2)).thenReturn(true);
        when(trainingTypeService.getReferenceById(2)).thenReturn(type);
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = trainerService.createTrainer(new TrainerCreateRequest("John", "Doe", 2));

        assertThat(result).isEqualTo(credentials);
        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getSpecialization()).isEqualTo(type);
    }

    @Test
    @DisplayName("createTrainer throws when specialization does not exist")
    void createTrainer_ThrowsWhenSpecializationMissing() {
        UserCredentialsDTO credentials = new UserCredentialsDTO(1L, "john.doe", "raw-password");
        User user = new User();
        user.setId(1L);
        when(userService.createUser(any())).thenReturn(credentials);
        when(userService.getReferenceById(1L)).thenReturn(user);
        when(trainingTypeService.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> trainerService.createTrainer(new TrainerCreateRequest("John", "Doe", 99)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("TrainingType not found with ID: 99");
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    @DisplayName("updateTrainer updates user and specialization")
    void updateTrainer_UpdatesTrainer() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setActive(true);

        TrainingType type = new TrainingType();
        type.setId(3);
        type.setName(TrainingTypeName.CARDIO);

        when(trainerRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainer));
        when(trainingTypeService.existsById(3)).thenReturn(true);
        when(trainingTypeService.getReferenceById(3)).thenReturn(type);
        when(trainerTraineeRelationService.findTraineesOfTrainerByTrainerUsername("john.doe")).thenReturn(List.of());

        TrainerProfileView result = trainerService.updateTrainer(new TrainerUpdateRequest("john.doe", "Johnny", "Smith", 3));

        assertThat(result.specializationId()).isEqualTo(3);
        verify(userService).updateUser(1L, new UserUpdateRequest("Johnny", "Smith"));
        verify(trainerRepository).save(trainer);
        assertThat(trainer.getSpecialization()).isEqualTo(type);
    }

    @Test
    @DisplayName("findTrainersNotAssignedToTrainee returns empty list when trainee does not exist")
    void findTrainersNotAssignedToTrainee_ReturnsEmptyListForMissingTrainee() {
        when(traineeRepository.existsByUserUsername("missing")).thenReturn(false);

        assertThat(trainerService.findTrainersNotAssignedToTrainee("missing")).isEmpty();
    }

    @Test
    @DisplayName("updateStatus deactivates an active trainer")
    void updateStatus_DeactivatesTrainer() {
        User user = new User();
        user.setUsername("john.doe");
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setActive(true);
        when(trainerRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainer));

        trainerService.updateStatus(new UpdateStatusRequest("john.doe", false));

        assertThat(trainer.isActive()).isFalse();
        verify(trainerRepository).save(trainer);
    }
}

