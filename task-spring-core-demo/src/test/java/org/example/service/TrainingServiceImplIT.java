package org.example.service;

import org.example.dao.TrainingRepository;
import org.example.dao.TrainingTypeRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.TrainerDTO;
import org.example.dto.response.TraineeDTO;
import org.example.dto.response.TrainingDTO;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.entity.User;
import org.example.service.api.PasswordEncoder;
import org.example.service.api.TrainerService;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainingService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TrainingService - Service Slice Integration Tests")
class TrainingServiceImplIT extends AbstractServiceSliceTest {

    private static final AuthRequest AUTHENTICATED_USER = new AuthRequest("it.auth.user", "it-pass");
    private static final AuthRequest ANONYMOUS_USER = new AuthRequest("anonymous.user", "anonymous-pass");

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private org.example.dao.UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void ensureAuthenticatedUserExists() {
        userRepository.findByUsername(AUTHENTICATED_USER.username())
                .orElseGet(() -> userRepository.save(user(AUTHENTICATED_USER.username(), AUTHENTICATED_USER.password())));
    }

    @Test
    @DisplayName("createTraining should persist training when trainee, trainer, type exist and relation is assigned")
    void createTraining_ShouldPersistTraining_WhenAllReferencesAndRelationExist() {
        int typeId = persistTrainingType(TrainingTypeName.CARDIO).getId();
        TraineeDTO trainee = traineeService.createTrainee(new TraineeCreateRequest("Train", "Ee", "Baku", LocalDate.of(2000, 1, 1)));
        TrainerDTO trainer = trainerService.createTrainer(new TrainerCreateRequest("Train", "Er", typeId));

        trainerService.reassignTraineeToTrainers(trainee.id(), List.of(trainer.id()), AUTHENTICATED_USER);

        TrainingDTO created = trainingService.createTraining(
                new TrainingCreateRequest(trainee.id(), trainer.id(), "Cardio Morning", typeId, LocalDate.now().plusDays(1), 60),
                AUTHENTICATED_USER
        );

        assertThat(created).isNotNull();
        assertThat(created.name()).isEqualTo("Cardio Morning");
        assertThat(trainingRepository.findById(created.id())).isPresent();
    }

    @Test
    @DisplayName("createTraining should throw RuntimeException when trainer is not assigned to trainee")
    void createTraining_ShouldThrowRuntimeException_WhenTrainerTraineeRelationMissing() {
        int typeId = persistTrainingType(TrainingTypeName.STRENGTH).getId();
        TraineeDTO trainee = traineeService.createTrainee(new TraineeCreateRequest("No", "Relation", "Baku", LocalDate.of(1999, 2, 2)));
        TrainerDTO trainer = trainerService.createTrainer(new TrainerCreateRequest("No", "Pair", typeId));

        assertThatThrownBy(() -> trainingService.createTraining(
                new TrainingCreateRequest(trainee.id(), trainer.id(), "No Pair Session", typeId, LocalDate.now().plusDays(1), 45),
                AUTHENTICATED_USER
        )).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("relation does not exists");
    }

    @Test
    @DisplayName("updateTraining should throw EntityNotFoundException when target trainee does not exist")
    void updateTraining_ShouldThrowEntityNotFoundException_WhenTargetTraineeDoesNotExist() {
        int typeId = persistTrainingType(TrainingTypeName.CARDIO).getId();
        TraineeDTO trainee = traineeService.createTrainee(new TraineeCreateRequest("Upd", "Trainee", "Baku", LocalDate.of(2001, 3, 3)));
        TrainerDTO trainer = trainerService.createTrainer(new TrainerCreateRequest("Upd", "Trainer", typeId));
        trainerService.reassignTraineeToTrainers(trainee.id(), List.of(trainer.id()), AUTHENTICATED_USER);

        TrainingDTO created = trainingService.createTraining(
                new TrainingCreateRequest(trainee.id(), trainer.id(), "Before Update", typeId, LocalDate.now().plusDays(1), 30),
                AUTHENTICATED_USER
        );

        assertThatThrownBy(() -> trainingService.updateTraining(
                created.id(),
                new TrainingUpdateRequest(999_999L, trainer.id(), "After Update", typeId, LocalDate.now().plusDays(2), 40),
                AUTHENTICATED_USER
        )).isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("TraineeId not found");
    }

    @Test
    @DisplayName("deleteTraining should return false when repository cannot soft-delete unknown id")
    void deleteTraining_ShouldReturnFalse_WhenTrainingIdDoesNotExist() {
        boolean deleted = trainingService.deleteTraining(999_999L, AUTHENTICATED_USER);

        assertThat(deleted).isFalse();
    }

    @Test
    @DisplayName("createTraining should throw SecurityException when anonymous credentials are provided")
    void createTraining_ShouldThrowSecurityException_WhenAnonymousCredentialsAreUsed() {
        int typeId = persistTrainingType(TrainingTypeName.CARDIO).getId();
        TraineeDTO trainee = traineeService.createTrainee(new TraineeCreateRequest("Anon", "Trainee", "Baku", LocalDate.of(2000, 1, 1)));
        TrainerDTO trainer = trainerService.createTrainer(new TrainerCreateRequest("Anon", "Trainer", typeId));

        assertThatThrownBy(() -> trainingService.createTraining(
                new TrainingCreateRequest(trainee.id(), trainer.id(), "Should Fail", typeId, LocalDate.now().plusDays(1), 30),
                ANONYMOUS_USER
        )).isInstanceOf(SecurityException.class)
          .hasMessageContaining("INvalid credentials");
    }

    private TrainingType persistTrainingType(TrainingTypeName name) {
        TrainingType type = new TrainingType();
        type.setName(name);
        return trainingTypeRepository.save(type);
    }

    private User user(String username, String password) {
        User user = new User();
        user.setFirstName("IT");
        user.setLastName("Auth");
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }
}

