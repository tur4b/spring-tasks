package org.example.dao;

import org.example.entity.*;
import org.example.testsupport.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainingRepository - DAO Slice Integration Tests")
class TrainingRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Test
    @DisplayName("findTrainingViewById - returns view for persisted training")
    void findTrainingViewById_ReturnsView() {
        Training t = persistTraining();

        assertThat(trainingRepository.findTrainingViewById(t.getId())).isPresent();
    }

    @Test
    @DisplayName("findTrainingViewById - returns empty optional when training id does not exist")
    void findTrainingViewById_ShouldReturnEmptyOptional_WhenTrainingIdMissing() {
        assertThat(trainingRepository.findTrainingViewById(999_999L)).isEmpty();
    }

    @Test
    @DisplayName("findAllTrainingsView - includes persisted training projections after each insert")
    void findAllTrainingsView_ShouldIncludePersistedTrainingProjections() {
        int before = trainingRepository.findAllTrainingsView().size();
        persistTraining();

        assertThat(trainingRepository.findAllTrainingsView()).hasSize(before + 1);
    }

    @Test
    @DisplayName("softDeleteById - sets active=false, returns 1")
    void softDeleteById_DeactivatesTraining() {
        Training t = persistTraining();

        assertThat(trainingRepository.softDeleteById(t.getId())).isEqualTo(1);
        assertThat(trainingRepository.findById(t.getId()))
                .isPresent().get().extracting(Training::isActive).isEqualTo(false);
    }

    @Test
    @DisplayName("softDeleteById - returns 0 for unknown id")
    void softDeleteById_UnknownId_ReturnsZero() {
        assertThat(trainingRepository.softDeleteById(99999L)).isZero();
    }

    // helper methods

    private Training persistTraining() {
        TrainingType type = persistTrainingType(TrainingTypeName.CARDIO);
        Trainee trainee = persistTrainee("tr.nee" + System.nanoTime());
        Trainer trainer = persistTrainer("tr.ner" + System.nanoTime(), type);

        Training training = new Training();
        training.setName("Session");
        training.setDate(LocalDate.now().plusDays(1));
        training.setDuration(60);
        training.setType(type);
        training.setTrainer(trainer);
        training.setTrainee(trainee);

        return trainingRepository.save(training);
    }

    private TrainingType persistTrainingType(TrainingTypeName typeName) {
        TrainingType type = new TrainingType();
        type.setName(typeName);
        return trainingTypeRepository.save(type);
    }

    private Trainee persistTrainee(String username) {
        User traineeUser = new User();
        traineeUser.setFirstName("Tr");
        traineeUser.setLastName("Nee");
        traineeUser.setUsername(username);
        traineeUser.setPassword("pw");

        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        trainee.setAddress("City");
        trainee.setDateOfBirth(LocalDate.of(2001, 1, 1));
        return traineeRepository.save(trainee);
    }

    private Trainer persistTrainer(String username, TrainingType type) {
        User trainerUser = new User();
        trainerUser.setFirstName("Tr");
        trainerUser.setLastName("Ner");
        trainerUser.setUsername(username);
        trainerUser.setPassword("pw");

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(type);
        return trainerRepository.save(trainer);
    }
}

