package org.example.dao;

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
@DisplayName("TrainingRepository Integration Tests")
class TrainingRepositoryIT {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DisplayName("softDeleteById marks the training as inactive")
    void softDeleteById_MarksTrainingInactive() {
        TrainingType type = new TrainingType();
        type.setName(TrainingTypeName.CARDIO);
        trainingTypeRepository.saveAndFlush(type);

        Trainee trainee = trainee("trainee.one", "Alice", "Smith");
        traineeRepository.saveAndFlush(trainee);

        Trainer trainer = trainer("trainer.one", "John", "Doe", type);
        trainerRepository.saveAndFlush(trainer);

        Training training = new Training();
        training.setName("Morning cardio");
        training.setDate(LocalDate.of(2026, 1, 1));
        training.setDuration(45);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setType(type);
        training.setActive(true);
        trainingRepository.saveAndFlush(training);

        int updatedRows = trainingRepository.softDeleteById(training.getId());

        assertThat(updatedRows).isEqualTo(1);
        assertThat(trainingRepository.findById(training.getId())).get().extracting(Training::isActive).isEqualTo(false);
    }

    private static Trainee trainee(String username, String firstName, String lastName) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("encoded-password");

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setAddress("Main street");
        trainee.setDateOfBirth(LocalDate.of(1995, 1, 10));
        trainee.setActive(true);
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

