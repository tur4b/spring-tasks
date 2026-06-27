package org.example.service;

import org.example.dao.TraineeRepository;
import org.example.dao.TrainerRepository;
import org.example.dto.request.TraineeUpdateTrainersRequest;
import org.example.dto.response.TraineeProfileTrainerDTO;
import org.example.dto.response.TrainerProfileTraineeDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerTraineeRelationServiceImpl Unit Tests")
class TrainerTraineeRelationServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @InjectMocks
    private TrainerTraineeRelationServiceImpl relationService;

    @Test
    @DisplayName("updateTraineeTrainers replaces assigned trainers and returns the new list")
    void updateTraineeTrainers_ReplacesTrainers() {
        Trainee trainee = new Trainee();
        trainee.setId(10L);
        User traineeUser = new User();
        traineeUser.setUsername("trainee.one");
        trainee.setUser(traineeUser);

        Trainer firstTrainer = trainer("trainer.one", "John", "Doe", 1);
        Trainer secondTrainer = trainer("trainer.two", "Jane", "Smith", 2);
        Trainer oldTrainer = trainer("trainer.old", "Old", "Trainer", 3);
        oldTrainer.getTrainees().add(trainee);
        trainee.getTrainers().add(oldTrainer);

        when(traineeRepository.findByUserUsername("trainee.one")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllByUsernames(List.of("trainer.one", "trainer.two"))).thenReturn(List.of(firstTrainer, secondTrainer));
        when(trainerRepository.findAllByTraineesId(10L)).thenReturn(List.of(oldTrainer));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(trainerRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<TraineeProfileTrainerDTO> result = relationService.updateTraineeTrainers(
                new TraineeUpdateTrainersRequest(
                        "trainee.one",
                        List.of(
                                new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer.one"),
                                new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer.two"),
                                new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer.one")
                        )
                )
        );

        assertThat(result).containsExactly(
                new TraineeProfileTrainerDTO("trainer.one", "John", "Doe", 1),
                new TraineeProfileTrainerDTO("trainer.two", "Jane", "Smith", 2)
        );
        assertThat(trainee.getTrainers()).containsExactly(firstTrainer, secondTrainer);
        assertThat(oldTrainer.getTrainees()).doesNotContain(trainee);
        verify(traineeRepository).save(trainee);
        verify(trainerRepository).saveAll(List.of(firstTrainer, secondTrainer));
        verify(trainerRepository).saveAll(List.of(oldTrainer));
    }

    @Test
    @DisplayName("updateTraineeTrainers throws when a trainer is missing")
    void updateTraineeTrainers_ThrowsWhenTrainerMissing() {
        Trainee trainee = new Trainee();
        trainee.setId(10L);
        User traineeUser = new User();
        traineeUser.setUsername("trainee.one");
        trainee.setUser(traineeUser);

        when(traineeRepository.findByUserUsername("trainee.one")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllByUsernames(List.of("trainer.one", "trainer.two"))).thenReturn(List.of(trainer("trainer.one", "John", "Doe", 1)));

        assertThatThrownBy(() -> relationService.updateTraineeTrainers(
                new TraineeUpdateTrainersRequest(
                        "trainee.one",
                        List.of(
                                new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer.one"),
                                new TraineeUpdateTrainersRequest.TrainerUsernameDTO("trainer.two")
                        )
                )
        )).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Some trainers not found");
    }

    @Test
    @DisplayName("findTrainersOfTraineeByTraineeUsername delegates to repository")
    void findTrainersOfTraineeByTraineeUsername_DelegatesToRepository() {
        List<TraineeProfileTrainerDTO> trainers = List.of(new TraineeProfileTrainerDTO("trainer.one", "John", "Doe", 1));
        when(trainerRepository.findTrainersOfTraineeByTraineeUsername("trainee.one")).thenReturn(trainers);

        assertThat(relationService.findTrainersOfTraineeByTraineeUsername("trainee.one")).isEqualTo(trainers);
    }

    @Test
    @DisplayName("findTraineesOfTrainerByTrainerUsername delegates to repository")
    void findTraineesOfTrainerByTrainerUsername_DelegatesToRepository() {
        List<TrainerProfileTraineeDTO> trainees = List.of(new TrainerProfileTraineeDTO("trainee.one", "Alice", "Smith"));
        when(trainerRepository.findTraineesOfTrainerByTrainerUsername("trainer.one")).thenReturn(trainees);

        assertThat(relationService.findTraineesOfTrainerByTrainerUsername("trainer.one")).isEqualTo(trainees);
    }

    private static Trainer trainer(String username, String firstName, String lastName, int specializationId) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        TrainingType type = new TrainingType();
        type.setId(specializationId);
        type.setName(TrainingTypeName.CARDIO);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(type);
        return trainer;
    }
}

