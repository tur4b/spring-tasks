package org.example.dao;

import org.example.entity.*;
import org.example.testsupport.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainerRepository - DAO Slice Integration Tests")
class TrainerRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    @DisplayName("findAllTrainersView - list grows after each save")
    void findAllTrainersView_GrowsAfterSave() {
        TrainingType trainingType = persistTrainingType(TrainingTypeName.CARDIO);
        int before = trainerRepository.findAllTrainersView().size();
        trainerRepository.save(trainer("new.trainer", trainingType));

        assertThat(trainerRepository.findAllTrainersView()).hasSize(before + 1);
    }

    @Test
    @DisplayName("existsByUserUsername - correct booleans")
    void existsByUserUsername() {
        TrainingType trainingType = persistTrainingType(TrainingTypeName.CARDIO);
        trainerRepository.save(trainer("exists.trainer", trainingType));

        assertThat(trainerRepository.existsByUserUsername("exists.trainer")).isTrue();
        assertThat(trainerRepository.existsByUserUsername("no.trainer")).isFalse();
    }

    @Test
    @DisplayName("existsTrainerTraineeRelation - true for linked pair, false otherwise")
    void existsTrainerTraineeRelation() {
        TrainingType trainingType = persistTrainingType(TrainingTypeName.CARDIO);
        Trainee trainee = persistTrainee("pair.trainee");
        Trainer trainer = trainer("pair.trainer", trainingType);
        trainer.getTrainees().add(trainee);
        Trainer saved = trainerRepository.save(trainer);

        assertThat(trainerRepository.existsTrainerTraineeRelation(saved.getId(), trainee.getId())).isTrue();
        assertThat(trainerRepository.existsTrainerTraineeRelation(saved.getId(), 999L)).isFalse();
    }

    @Test
    @DisplayName("findTrainersNotAssignedToTrainee - excludes assigned trainer")
    void findTrainersNotAssignedToTrainee_ExcludesAssigned() {
        TrainingType trainingType = persistTrainingType(TrainingTypeName.CARDIO);
        Trainee trainee = persistTrainee("filter.trainee");
        Trainer assigned = trainer("assigned.trainer", trainingType);
        assigned.getTrainees().add(trainee);
        Trainer unassigned = trainer("free.trainer", trainingType);

        trainerRepository.save(assigned);
        trainerRepository.save(unassigned);

        assertThat(trainerRepository.findTrainersNotAssignedToTrainee("filter.trainee"))
                .noneMatch(v -> v.getFirstName().equals("Assigned"));
    }

    @Test
    @DisplayName("findAllByTraineesId should return only trainers assigned to the requested trainee id")
    void findAllByTraineesId_ShouldReturnOnlyAssignedTrainersForGivenTraineeId() {
        TrainingType trainingType = persistTrainingType(TrainingTypeName.STRENGTH);
        Trainee assignedTrainee = persistTrainee("assigned.trainee");
        Trainee otherTrainee = persistTrainee("other.trainee");

        Trainer assignedTrainer = trainer("assigned.lookup", trainingType);
        assignedTrainer.getTrainees().add(assignedTrainee);
        Trainer otherTrainer = trainer("other.lookup", trainingType);
        otherTrainer.getTrainees().add(otherTrainee);

        trainerRepository.save(assignedTrainer);
        trainerRepository.save(otherTrainer);

        assertThat(trainerRepository.findAllByTraineesId(assignedTrainee.getId()))
                .extracting(Trainer::getUser)
                .extracting(User::getUsername)
                .containsExactly("assigned.lookup")
                .doesNotContain("other.lookup");
    }

    @Test
    @DisplayName("findTrainingsOfTrainerByCriteria should return only trainings that satisfy date range, trainee name and type")
    void findTrainingsOfTrainerByCriteria_ShouldApplyAllProvidedFilters() {
        TrainingType cardio = persistTrainingType(TrainingTypeName.CARDIO);
        TrainingType strength = persistTrainingType(TrainingTypeName.STRENGTH);
        Trainer trainer = trainerRepository.save(trainer("criteria.trainer", cardio));
        Trainee targetTrainee = persistTrainee("criteria.trainee");
        Trainee otherTrainee = persistTrainee("other.criteria.trainee");

        persistTraining("Match Training", trainer, targetTrainee, cardio, LocalDate.now().plusDays(1), 55);
        persistTraining("Non Match Training", trainer, otherTrainee, strength, LocalDate.now().plusDays(2), 35);

        assertThat(trainerRepository.findTrainingsOfTrainerByCriteria(
                "criteria.trainer",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "first last",
                cardio.getId()))
                .hasSize(1)
                .allMatch(view -> view.getName().equals("Match Training"));
    }

    // helper methoods

    private TrainingType persistTrainingType(TrainingTypeName typeName) {
        TrainingType type = new TrainingType();
        type.setName(typeName);
        trainingTypeRepository.save(type);
        return type;
    }
    private Trainer trainer(String username, TrainingType type) {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setUsername(username);
        user.setPassword("pass");

        Trainer tr = new Trainer();
        tr.setUser(user);
        tr.setSpecialization(type);
        return tr;
    }

    private Trainee persistTrainee(String username) {
        User user = new User();
        user.setFirstName("First");
        user.setLastName("Last");
        user.setUsername(username);
        user.setPassword("pass");

        Trainee t = new Trainee();
        t.setUser(user);
        t.setAddress("Baku");
        t.setDateOfBirth(LocalDate.of(2000, 1, 1));

        traineeRepository.save(t);
        return t;
    }

    private Training persistTraining(String name,
                                    Trainer trainer,
                                    Trainee trainee,
                                    TrainingType type,
                                    LocalDate date,
                                    int duration) {
        Training training = new Training();
        training.setName(name);
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        training.setType(type);
        training.setDate(date);
        training.setDuration(duration);
        return trainingRepository.save(training);
    }
}

