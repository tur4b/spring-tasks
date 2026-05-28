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

    // ─── save + findById (inherited JPA) ─────────────────────────────────────

    @Test
    @DisplayName("save + findById - persists training and retrieves by ID")
    void save_PersistsTraining_FindById_ReturnsIt() {
        Training t = persistTraining();

        assertThat(trainingRepository.findById(t.getId())).isPresent();
    }

    @Test
    @DisplayName("findById - returns empty Optional for unknown ID")
    void findById_ReturnsEmpty_ForUnknownId() {
        assertThat(trainingRepository.findById(999_999L)).isEmpty();
    }

    // ─── softDeleteById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("softDeleteById - sets active=false and returns 1 affected row")
    void softDeleteById_DeactivatesTraining() {
        Training t = persistTraining();

        assertThat(trainingRepository.softDeleteById(t.getId())).isEqualTo(1);
        assertThat(trainingRepository.findById(t.getId()))
                .isPresent()
                .get()
                .extracting(Training::isActive)
                .isEqualTo(false);
    }

    @Test
    @DisplayName("softDeleteById - returns 0 for unknown id")
    void softDeleteById_UnknownId_ReturnsZero() {
        assertThat(trainingRepository.softDeleteById(99_999L)).isZero();
    }

    // ─── helper methods ───────────────────────────────────────────────────────

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
        User u = new User();
        u.setFirstName("Tr");
        u.setLastName("Nee");
        u.setUsername(username);
        u.setPassword("pw");

        Trainee t = new Trainee();
        t.setUser(u);
        t.setAddress("City");
        t.setDateOfBirth(LocalDate.of(2001, 1, 1));
        return traineeRepository.save(t);
    }

    private Trainer persistTrainer(String username, TrainingType type) {
        User u = new User();
        u.setFirstName("Tr");
        u.setLastName("Ner");
        u.setUsername(username);
        u.setPassword("pw");

        Trainer tr = new Trainer();
        tr.setUser(u);
        tr.setSpecialization(type);
        return trainerRepository.save(tr);
    }
}
