package org.example.dao;

import org.example.dto.response.TraineeDTO;
import org.example.dto.response.TraineeTrainingProfileView;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("TraineeRepository Integration Tests")
class TraineeRepositoryIT {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DisplayName("findTraineeDTOByUsername returns active trainee projection")
    void findTraineeDTOByUsername_ReturnsProjection() {
        Trainee trainee = trainee("alice.smith", "Alice", "Smith", true);
        traineeRepository.saveAndFlush(trainee);

        TraineeDTO result = traineeRepository.findTraineeDTOByUsername("alice.smith").orElseThrow();

        assertThat(result.firstName()).isEqualTo("Alice");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("findTrainingsOfTraineeByCriteria returns matching trainings")
    void findTrainingsOfTraineeByCriteria_ReturnsMatchingTrainings() {
        TrainingType trainingType = new TrainingType();
        trainingType.setName(TrainingTypeName.CARDIO);
        trainingTypeRepository.saveAndFlush(trainingType);

        Trainee trainee = trainee("alice.smith", "Alice", "Smith", true);
        traineeRepository.saveAndFlush(trainee);

        Trainer trainer = trainer("trainer.one", "John", "Doe", trainingType);
        trainer.getTrainees().add(trainee);
        trainee.getTrainers().add(trainer);
        trainerRepository.saveAndFlush(trainer);
        traineeRepository.saveAndFlush(trainee);

        Training training = new Training();
        training.setName("Morning cardio");
        training.setDate(LocalDate.of(2026, 1, 1));
        training.setDuration(45);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setType(trainingType);
        trainingRepository.saveAndFlush(training);

        var result = traineeRepository.findTrainingsOfTraineeByCriteria("alice.smith", null, null, null, trainingType.getId());

        assertThat(result).containsExactly(new TraineeTrainingProfileView("Morning cardio", LocalDate.of(2026, 1, 1), trainingType.getId(), 45, "John Doe"));
    }

    @Test
    @DisplayName("deleteByUserUsername removes the trainee by username")
    void deleteByUserUsername_RemovesTrainee() {
        Trainee trainee = trainee("alice.smith", "Alice", "Smith", true);
        traineeRepository.saveAndFlush(trainee);

        traineeRepository.deleteByUserUsername("alice.smith");

        assertThat(traineeRepository.findByUserUsername("alice.smith")).isEmpty();
    }

    private static Trainee trainee(String username, String firstName, String lastName, boolean active) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("encoded-password");
        user.setActive(active);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setAddress("Main street");
        trainee.setDateOfBirth(LocalDate.of(1995, 1, 10));
        trainee.setActive(active);
        return trainee;
    }

    private static Trainer trainer(String username, String firstName, String lastName, TrainingType specialization) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("encoded-password");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);
        trainer.setActive(true);
        return trainer;
    }
}

