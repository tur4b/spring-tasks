package org.example.service;

import org.example.config.security.JwtService;
import org.example.dao.TrainingTypeRepository;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TraineeUpdateTrainersRequest;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.request.UpdateStatusRequest;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.service.api.AuthService;
import org.example.service.api.TraineeService;
import org.example.service.api.TrainerService;
import org.example.service.api.TrainerTraineeRelationService;
import org.example.service.api.TrainingService;
import org.example.service.api.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Service Integration Tests")
class ServiceIntegrationIT {

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TrainerTraineeRelationService trainerTraineeRelationService;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("trainer service creates a trainer and can load the profile back")
    void trainerServiceCreatesTrainerAndLoadsProfile() {
        TrainingType type = trainingTypeRepository.saveAndFlush(trainingType(TrainingTypeName.CARDIO));

        var credentials = trainerService.createTrainer(new TrainerCreateRequest("John", "Doe", type.getId()));

        assertThat(credentials.username()).isEqualTo("john.doe");
        assertThat(trainerService.findTrainerViewByUsername(credentials.username()).firstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("trainee service creates a trainee and status updates persist")
    void traineeServiceCreatesTraineeAndStatusUpdatesPersist() {
        var credentials = traineeService.createTrainee(new TraineeCreateRequest("Alice", "Smith", "Main street", LocalDate.of(1995, 1, 10)));

        assertThat(traineeService.existsByUsername(credentials.username())).isTrue();
        traineeService.updateStatus(new UpdateStatusRequest(credentials.username(), false));
        assertThat(traineeService.findTraineeByUsername(credentials.username()).isActive()).isFalse();
    }

    @Test
    @DisplayName("training service creates a training for an assigned trainer and trainee")
    void trainingServiceCreatesTrainingForAssignedTrainerAndTrainee() {
        TrainingType type = trainingTypeRepository.saveAndFlush(trainingType(TrainingTypeName.STRENGTH));
        var trainerCredentials = trainerService.createTrainer(new TrainerCreateRequest("John", "Doe", type.getId()));
        var traineeCredentials = traineeService.createTrainee(new TraineeCreateRequest("Alice", "Smith", "Main street", LocalDate.of(1995, 1, 10)));

        trainerTraineeRelationService.updateTraineeTrainers(new TraineeUpdateTrainersRequest(
                traineeCredentials.username(),
                List.of(new TraineeUpdateTrainersRequest.TrainerUsernameDTO(trainerCredentials.username()))
        ));

        var training = trainingService.createTraining(new TrainingCreateRequest(
                traineeCredentials.username(),
                trainerCredentials.username(),
                "Morning strength",
                LocalDate.now(),
                45
        ));

        assertThat(training.name()).isEqualTo("Morning strength");
        assertThat(training.duration()).isEqualTo(45);
    }

    @Test
    @DisplayName("auth service logs in a created user and changes password")
    void authServiceLogsInAndChangesPassword() {
        var credentials = userService.createUser(new org.example.dto.request.UserCreateRequest("John", "Doe"));

        String token = authService.login(new AuthRequest(credentials.username(), credentials.password()));
        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();

        authService.changePassword(new ChangePasswordRequest(credentials.username(), credentials.password(), "new-secret"));

        String storedPassword = userRepository.findByUsername(credentials.username()).orElseThrow().getPassword();
        assertThat(passwordEncoder.matches("new-secret", storedPassword)).isTrue();
    }

    private static TrainingType trainingType(TrainingTypeName name) {
        TrainingType trainingType = new TrainingType();
        trainingType.setName(name);
        return trainingType;
    }
}

