package org.example.dao;

import org.example.dto.response.TraineeProfileTrainerDTO;
import org.example.dto.response.TrainerDTO;
import org.example.dto.response.TrainerProfileTraineeDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("TrainerRepository Integration Tests")
class TrainerRepositoryIT {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DisplayName("findTrainerDTOByUsername returns active trainer projection")
    void findTrainerDTOByUsername_ReturnsProjection() {
        TrainingType type = trainingType();
        Trainer trainer = trainer("john.doe", "John", "Doe", type);
        trainerRepository.saveAndFlush(trainer);

        TrainerDTO result = trainerRepository.findTrainerDTOByUsername("john.doe").orElseThrow();

        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.specializationId()).isEqualTo(type.getId());
    }

    @Test
    @DisplayName("existsTrainerTraineeRelation and trainer search queries reflect persisted relations")
    void relationQueries_ReturnPersistedRelations() {
        TrainingType type = trainingType();
        Trainer assignedTrainer = trainer("trainer.one", "John", "Doe", type);
        Trainer freeTrainer = trainer("trainer.two", "Jane", "Smith", type);
        Trainee trainee = trainee("trainee.one", "Alice", "Brown");

        trainerRepository.saveAndFlush(assignedTrainer);
        trainerRepository.saveAndFlush(freeTrainer);
        traineeRepository.saveAndFlush(trainee);

        assignedTrainer.getTrainees().add(trainee);
        trainee.getTrainers().add(assignedTrainer);
        trainerRepository.saveAndFlush(assignedTrainer);
        traineeRepository.saveAndFlush(trainee);

        assertThat(trainerRepository.existsTrainerTraineeRelation("trainer.one", "trainee.one")).isTrue();
        assertThat(trainerRepository.existsTrainerTraineeRelation("trainer.two", "trainee.one")).isFalse();

        List<TraineeProfileTrainerDTO> unassigned = trainerRepository.findTrainersNotAssignedToTrainee("trainee.one");
        assertThat(unassigned).extracting(TraineeProfileTrainerDTO::username).containsExactly("trainer.two");

        List<TrainerProfileTraineeDTO> trainees = trainerRepository.findTraineesOfTrainerByTrainerUsername("trainer.one");
        assertThat(trainees).extracting(TrainerProfileTraineeDTO::username).containsExactly("trainee.one");
    }

    private TrainingType trainingType() {
        TrainingType type = new TrainingType();
        type.setName(TrainingTypeName.CARDIO);
        trainingTypeRepository.saveAndFlush(type);
        return type;
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
}

