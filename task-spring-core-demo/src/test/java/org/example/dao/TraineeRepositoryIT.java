package org.example.dao;

import org.example.dto.response.TraineeDTO;
import org.example.dto.response.TraineeTrainingProfileView;
import org.example.entity.Trainee;
import org.example.entity.Trainer;
import org.example.entity.Training;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.entity.User;
import org.example.testsupport.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TraineeRepository - DAO Slice Integration Tests")
class TraineeRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    // ─── findByUserUsername ──────────────────────────────────────────────────

    @Test
    @DisplayName("findByUserUsername - returns present Optional after save")
    void findByUserUsername_ReturnsTrainee() {
        traineeRepository.save(trainee("findby.username"));

        assertThat(traineeRepository.findByUserUsername("findby.username")).isPresent();
    }

    @Test
    @DisplayName("findByUserUsername - returns empty Optional for unknown username")
    void findByUserUsername_ReturnsEmpty_WhenNotFound() {
        assertThat(traineeRepository.findByUserUsername("ghost.user")).isEmpty();
    }

    // ─── existsByUserUsername ────────────────────────────────────────────────

    @Test
    @DisplayName("existsByUserUsername - true for saved trainee, false for unknown")
    void existsByUserUsername_CorrectBooleans() {
        traineeRepository.save(trainee("present.user"));

        assertThat(traineeRepository.existsByUserUsername("present.user")).isTrue();
        assertThat(traineeRepository.existsByUserUsername("absent.user")).isFalse();
    }

    // ─── findTraineeDTOByUsername ────────────────────────────────────────────

    @Test
    @DisplayName("findTraineeDTOByUsername - returns DTO when trainee is active")
    void findTraineeDTOByUsername_ReturnsDTOForActiveTrainee() {
        traineeRepository.save(trainee("dto.user"));

        assertThat(traineeRepository.findTraineeDTOByUsername("dto.user"))
                .isPresent()
                .get()
                .extracting(TraineeDTO::isActive)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("findTraineeDTOByUsername - returns empty Optional when trainee is inactive")
    void findTraineeDTOByUsername_ReturnsEmpty_WhenInactive() {
        Trainee t = trainee("inactive.dto.user");
        t.setActive(false);
        traineeRepository.save(t);

        assertThat(traineeRepository.findTraineeDTOByUsername("inactive.dto.user")).isEmpty();
    }

    // ─── deleteByUserUsername ────────────────────────────────────────────────

    @Test
    @DisplayName("deleteByUserUsername - removes trainee so existsByUserUsername returns false")
    void deleteByUserUsername_RemovesTrainee() {
        traineeRepository.save(trainee("todelete.user"));
        assertThat(traineeRepository.existsByUserUsername("todelete.user")).isTrue();

        traineeRepository.deleteByUserUsername("todelete.user");

        assertThat(traineeRepository.existsByUserUsername("todelete.user")).isFalse();
    }

    // ─── findTrainingsOfTraineeByCriteria ────────────────────────────────────

    @Test
    @DisplayName("findTrainingsOfTraineeByCriteria - returns only trainings matching all filters")
    void findTrainingsOfTraineeByCriteria_AppliesAllFilters() {
        Trainee trainee = traineeRepository.save(trainee("criteria.trainee"));
        TrainingType cardio = persistTrainingType(TrainingTypeName.CARDIO);
        TrainingType strength = persistTrainingType(TrainingTypeName.STRENGTH);
        Trainer matchTrainer = persistTrainer("fit.coach", "Fit", "Coach", cardio);
        Trainer otherTrainer = persistTrainer("other.coach", "Other", "Coach", strength);

        persistTraining(trainee, matchTrainer, cardio, "Match Session", LocalDate.now().plusDays(1), 45);
        persistTraining(trainee, otherTrainer, strength, "Non Match", LocalDate.now().plusDays(2), 30);

        List<TraineeTrainingProfileView> results = traineeRepository.findTrainingsOfTraineeByCriteria(
                "criteria.trainee",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "fit coach",
                cardio.getId());

        assertThat(results)
                .hasSize(1)
                .allMatch(v -> v.name().equals("Match Session"));
    }

    @Test
    @DisplayName("findTrainingsOfTraineeByCriteria - blank trainer name returns all within date range")
    void findTrainingsOfTraineeByCriteria_IgnoresBlankTrainerName() {
        Trainee trainee = traineeRepository.save(trainee("blank.filter.trainee"));
        TrainingType cardio = persistTrainingType(TrainingTypeName.CARDIO);
        Trainer first = persistTrainer("first.trainer2", "First", "Trainer", cardio);
        Trainer second = persistTrainer("second.trainer2", "Second", "Trainer", cardio);

        persistTraining(trainee, first, cardio, "First Session", LocalDate.now().plusDays(1), 40);
        persistTraining(trainee, second, cardio, "Second Session", LocalDate.now().plusDays(2), 50);

        List<TraineeTrainingProfileView> results = traineeRepository.findTrainingsOfTraineeByCriteria(
                "blank.filter.trainee",
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                "",
                cardio.getId());

        assertThat(results).hasSize(2);
    }

    // ─── helper methods ──────────────────────────────────────────────────────

    private Trainee trainee(String username) {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setUsername(username);
        user.setPassword("pass");

        Trainee t = new Trainee();
        t.setUser(user);
        t.setAddress("Baku");
        t.setDateOfBirth(LocalDate.of(2000, 1, 1));
        return t;
    }

    private TrainingType persistTrainingType(TrainingTypeName typeName) {
        TrainingType type = new TrainingType();
        type.setName(typeName);
        return trainingTypeRepository.save(type);
    }

    private Trainer persistTrainer(String username, String firstName, String lastName, TrainingType type) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword("pass");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(type);
        return trainerRepository.save(trainer);
    }

    private Training persistTraining(Trainee trainee, Trainer trainer, TrainingType type,
                                     String name, LocalDate date, int duration) {
        Training training = new Training();
        training.setName(name);
        training.setDate(date);
        training.setDuration(duration);
        training.setType(type);
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        return trainingRepository.save(training);
    }
}
