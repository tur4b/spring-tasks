package org.example.service;

import org.example.dao.TraineeRepository;
import org.example.dao.TrainerRepository;
import org.example.dao.TrainingRepository;
import org.example.dao.TrainingTypeRepository;
import org.example.dto.request.TrainingCreateRequest;
import org.example.dto.response.TrainingDTO;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.service.api.TrainingService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrainingService - Service Slice Integration Tests")
class TrainingServiceImplIT extends AbstractServiceSliceTest {

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    // ─── createTraining ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createTraining - throws NotFoundException when relation does not exist")
    void createTraining_NoRelation_ThrowsNotFoundException() {
        TrainingType type = persistTrainingType(TrainingTypeName.CARDIO);
        persistTrainer("trainer1", type);
        persistTrainee("trainee1");

        TrainingCreateRequest request = new TrainingCreateRequest(
                "trainee1", "trainer1", "Training", LocalDate.now(), 60);

        assertThatThrownBy(() -> trainingService.createTraining(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("relation does not exists");
    }

    @Test
    @DisplayName("createTraining - creates training when relation exists")
    void createTraining_WithRelation_Success() {
        TrainingType type = persistTrainingType(TrainingTypeName.STRENGTH);
        Trainer trainer = persistTrainer("trainer2", type);
        Trainee trainee = persistTrainee("trainee2");
        // Establish relationship
        trainer.getTrainees().add(trainee);
        trainee.getTrainers().add(trainer);
        trainerRepository.save(trainer);
        traineeRepository.save(trainee);

        TrainingCreateRequest request = new TrainingCreateRequest(
                "trainee2", "trainer2", "Strength Training", LocalDate.now().plusDays(1), 45);

        TrainingDTO result = trainingService.createTraining(request);

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Strength Training");
        assertThat(result.duration()).isEqualTo(45);
        assertThat(result.trainerId()).isEqualTo(trainer.getId());
        assertThat(result.traineeId()).isEqualTo(trainee.getId());
    }

    // ─── deleteTraining ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTraining - soft deletes training and returns true")
    void deleteTraining_ExistingTraining_ReturnsTrue() {
        Training training = persistTraining();

        boolean result = trainingService.deleteTraining(training.getId());

        assertThat(result).isTrue();
        // Soft delete - record still exists but with deleted flag
    }

    @Test
    @DisplayName("deleteTraining - returns false for non-existent training")
    void deleteTraining_NonExistent_ReturnsFalse() {
        boolean result = trainingService.deleteTraining(99999L);

        assertThat(result).isFalse();
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private Training persistTraining() {
        TrainingType type = persistTrainingType(TrainingTypeName.CARDIO);
        Trainer trainer = persistTrainer("trainer.training", type);
        Trainee trainee = persistTrainee("trainee.training");

        Training training = new Training();
        training.setName("Test Training");
        training.setDate(LocalDate.now());
        training.setDuration(60);
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setType(type);

        return trainingRepository.save(training);
    }

    private Trainer persistTrainer(String username, TrainingType specialization) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Trainer");
        user.setLastName("User");
        user.setPassword("encoded");
        user.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);
        trainer.setActive(true);

        return trainerRepository.save(trainer);
    }

    private Trainee persistTrainee(String username) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Trainee");
        user.setLastName("User");
        user.setPassword("encoded");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        trainee.setAddress("Baku");
        trainee.setActive(true);

        return traineeRepository.save(trainee);
    }

    private TrainingType persistTrainingType(TrainingTypeName name) {
        TrainingType type = new TrainingType();
        type.setName(name);
        return trainingTypeRepository.save(type);
    }
}

