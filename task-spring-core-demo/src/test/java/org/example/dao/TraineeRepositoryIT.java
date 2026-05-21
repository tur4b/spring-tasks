package org.example.dao;

import org.example.dao.projection.TrainingView;
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

    @Test
    @DisplayName("findByUserUsername - returns trainee after save")
    void findByUserUsername_ReturnsTrainee() {
        traineeRepository.save(trainee("trainee.one"));

        assertThat(traineeRepository.findByUserUsername("trainee.one")).isPresent();
    }

    @Test
    @DisplayName("existsByUserUsername - returns correct booleans")
    void existsByUserUsername() {
        traineeRepository.save(trainee("present.user"));

        assertThat(traineeRepository.existsByUserUsername("present.user")).isTrue();
        assertThat(traineeRepository.existsByUserUsername("absent.user")).isFalse();
    }

    @Test
    @DisplayName("findAllTraineesView - includes newly saved trainee")
    void findAllTraineesView_IncludesNewTrainee() {
        traineeRepository.save(trainee("view.user"));

        assertThat(traineeRepository.findAllTraineesView())
                .isNotEmpty()
                .anyMatch(v -> v.getFirstName().equals("First"));
    }

    @Test
    @DisplayName("findTraineeViewById - returns view for known id")
    void findTraineeViewById_ReturnsView() {
        Trainee saved = traineeRepository.save(trainee("by.id.user"));

        assertThat(traineeRepository.findTraineeViewById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("findTrainingsOfTraineeByCriteria should return only trainings matching date, trainer name, and type filters")
    void findTrainingsOfTraineeByCriteria_ShouldApplyAllProvidedFilters() {
        Trainee trainee = traineeRepository.save(trainee("criteria.trainee"));
        TrainingType cardio = persistTrainingType(TrainingTypeName.CARDIO);
        TrainingType strength = persistTrainingType(TrainingTypeName.STRENGTH);
        Trainer matchingTrainer = persistTrainer("fit.coach", "Fit", "Coach", cardio);
        Trainer otherTrainer = persistTrainer("other.coach", "Other", "Coach", strength);

        persistTraining(trainee, matchingTrainer, cardio, "Match Session", LocalDate.now().plusDays(1), 45);
        persistTraining(trainee, otherTrainer, strength, "Non Match Session", LocalDate.now().plusDays(2), 30);

        assertThat(traineeRepository.findTrainingsOfTraineeByCriteria(
                "criteria.trainee",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "fit coach",
                cardio.getId()))
                .hasSize(1)
                .allMatch(view -> view.getName().equals("Match Session"));
    }

    @Test
    @DisplayName("findTrainingsOfTraineeByCriteria should ignore trainer-name filter when blank and include all matching dates")
    void findTrainingsOfTraineeByCriteria_ShouldIgnoreTrainerNameFilter_WhenBlank() {
        Trainee trainee = traineeRepository.save(trainee("blank.filter.trainee"));
        TrainingType cardio = persistTrainingType(TrainingTypeName.CARDIO);
        Trainer firstTrainer = persistTrainer("first.trainer", "First", "Trainer", cardio);
        Trainer secondTrainer = persistTrainer("second.trainer", "Second", "Trainer", cardio);

        persistTraining(trainee, firstTrainer, cardio, "First Session", LocalDate.now().plusDays(1), 40);
        persistTraining(trainee, secondTrainer, cardio, "Second Session", LocalDate.now().plusDays(2), 50);

        assertThat(traineeRepository.findTrainingsOfTraineeByCriteria(
                "blank.filter.trainee",
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                "",
                cardio.getId()))
                .extracting(TrainingView::getName)
                .containsExactly("Second Session", "First Session");
    }

    // helper methods

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

    private Training persistTraining(Trainee trainee,
                                    Trainer trainer,
                                    TrainingType type,
                                    String name,
                                    LocalDate date,
                                    int duration) {
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

