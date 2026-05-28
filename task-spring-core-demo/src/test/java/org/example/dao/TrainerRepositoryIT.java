package org.example.dao;

import org.example.dto.response.TraineeProfileTrainerDTO;
import org.example.dto.response.TrainerDTO;
import org.example.dto.response.TrainerProfileTraineeDTO;
import org.example.dto.response.TrainerTrainingProfileView;
import org.example.entity.*;
import org.example.testsupport.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

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
    @DisplayName("existsByUserUsername - true for saved trainer, false for unknown")
    void existsByUserUsername_CorrectBooleans() {
        TrainingType type = persistTrainingType(TrainingTypeName.CARDIO);
        trainerRepository.save(trainer("exists.trainer", type));

        assertThat(trainerRepository.existsByUserUsername("exists.trainer")).isTrue();
        assertThat(trainerRepository.existsByUserUsername("no.trainer")).isFalse();
    }

    @Test
    @DisplayName("findTrainerDTOByUsername - returns DTO for saved trainer")
    void findTrainerDTOByUsername_ReturnsDTOForSavedTrainer() {
        TrainingType type = persistTrainingType(TrainingTypeName.STRENGTH);
        trainerRepository.save(trainer("dto.trainer", type));

        assertThat(trainerRepository.findTrainerDTOByUsername("dto.trainer"))
                .isPresent()
                .get()
                .extracting(TrainerDTO::firstName)
                .isEqualTo("First");
    }

    @Test
    @DisplayName("findTrainerDTOByUsername - returns empty Optional for unknown username")
    void findTrainerDTOByUsername_ReturnsEmpty_WhenNotFound() {
        assertThat(trainerRepository.findTrainerDTOByUsername("ghost.trainer")).isEmpty();
    }

    @Test
    @DisplayName("existsTrainerTraineeRelation - true for linked pair (uses usernames), false otherwise")
    void existsTrainerTraineeRelation_ByUsernames() {
        TrainingType type = persistTrainingType(TrainingTypeName.CARDIO);
        Trainee trainee = persistTrainee("pair.trainee");
        Trainer tr = trainer("pair.trainer", type);
        tr.getTrainees().add(trainee);
        trainerRepository.save(tr);

        assertThat(trainerRepository.existsTrainerTraineeRelation("pair.trainer", "pair.trainee")).isTrue();
        assertThat(trainerRepository.existsTrainerTraineeRelation("pair.trainer", "other.trainee")).isFalse();
    }

    // ─── findTrainersNotAssignedToTrainee ────────────────────────────────────

    @Test
    @DisplayName("findTrainersNotAssignedToTrainee - excludes assigned trainer")
    void findTrainersNotAssignedToTrainee_ExcludesAssigned() {
        TrainingType type = persistTrainingType(TrainingTypeName.CARDIO);
        Trainee trainee = persistTrainee("filter.trainee");
        Trainer assigned = trainer("assigned.trainer", type);
        assigned.getTrainees().add(trainee);
        Trainer unassigned = trainer("free.trainer", type);

        trainerRepository.save(assigned);
        trainerRepository.save(unassigned);

        assertThat(trainerRepository.findTrainersNotAssignedToTrainee("filter.trainee"))
                .extracting(TraineeProfileTrainerDTO::username)
                .doesNotContain("assigned.trainer")
                .contains("free.trainer");
    }

    // ─── findAllByTraineesId ─────────────────────────────────────────────────

    @Test
    @DisplayName("findAllByTraineesId - returns only trainers assigned to the requested trainee id")
    void findAllByTraineesId_ReturnsOnlyAssignedTrainers() {
        TrainingType type = persistTrainingType(TrainingTypeName.STRENGTH);
        Trainee assignedTrainee = persistTrainee("assigned.trainee");
        Trainee otherTrainee = persistTrainee("other.trainee");

        Trainer assignedTrainer = trainer("assigned.lookup", type);
        assignedTrainer.getTrainees().add(assignedTrainee);
        Trainer otherTrainer = trainer("other.lookup", type);
        otherTrainer.getTrainees().add(otherTrainee);

        trainerRepository.save(assignedTrainer);
        trainerRepository.save(otherTrainer);

        assertThat(trainerRepository.findAllByTraineesId(assignedTrainee.getId()))
                .extracting(t -> t.getUser().getUsername())
                .containsExactly("assigned.lookup")
                .doesNotContain("other.lookup");
    }

    // ─── findTrainersOfTraineeByTraineeUsername ───────────────────────────────

    @Test
    @DisplayName("findTrainersOfTraineeByTraineeUsername - returns only trainers linked to given trainee")
    void findTrainersOfTraineeByTraineeUsername_ReturnsLinkedTrainers() {
        TrainingType type = persistTrainingType(TrainingTypeName.CARDIO);
        Trainee trainee = persistTrainee("linked.trainee");
        Trainer linked = trainer("linked.trainer", type);
        linked.getTrainees().add(trainee);
        Trainer unlinked = trainer("unlinked.trainer", type);

        trainerRepository.save(linked);
        trainerRepository.save(unlinked);

        List<TraineeProfileTrainerDTO> result =
                trainerRepository.findTrainersOfTraineeByTraineeUsername("linked.trainee");

        assertThat(result)
                .extracting(TraineeProfileTrainerDTO::username)
                .containsExactly("linked.trainer");
    }

    // ─── findTraineesOfTrainerByTrainerUsername ───────────────────────────────

    @Test
    @DisplayName("findTraineesOfTrainerByTrainerUsername - returns trainees assigned to given trainer")
    void findTraineesOfTrainerByTrainerUsername_ReturnsLinkedTrainees() {
        TrainingType type = persistTrainingType(TrainingTypeName.STRENGTH);
        Trainee trainee = persistTrainee("my.trainee");
        Trainer tr = trainer("my.trainer", type);
        tr.getTrainees().add(trainee);
        trainerRepository.save(tr);

        List<TrainerProfileTraineeDTO> result =
                trainerRepository.findTraineesOfTrainerByTrainerUsername("my.trainer");

        assertThat(result)
                .extracting(TrainerProfileTraineeDTO::username)
                .containsExactly("my.trainee");
    }

    // ─── findTrainingsOfTrainerByCriteria ────────────────────────────────────

    @Test
    @DisplayName("findTrainingsOfTrainerByCriteria - applies date, trainee name, and type filters")
    void findTrainingsOfTrainerByCriteria_AppliesAllFilters() {
        TrainingType cardio = persistTrainingType(TrainingTypeName.CARDIO);
        TrainingType strength = persistTrainingType(TrainingTypeName.STRENGTH);
        Trainer tr = trainerRepository.save(trainer("crit.trainer", cardio));
        Trainee target = persistTrainee("crit.trainee");
        Trainee other = persistTrainee("other.crit.trainee");

        persistTraining("Match Training", tr, target, cardio, LocalDate.now().plusDays(1), 55);
        persistTraining("No-Match Training", tr, other, strength, LocalDate.now().plusDays(2), 35);

        List<TrainerTrainingProfileView> results = trainerRepository.findTrainingsOfTrainerByCriteria(
                "crit.trainer",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "first last",
                cardio.getId());

        assertThat(results)
                .hasSize(1)
                .allMatch(v -> v.name().equals("Match Training"));
    }

    // ─── helper methods ──────────────────────────────────────────────────────

    private TrainingType persistTrainingType(TrainingTypeName typeName) {
        TrainingType type = new TrainingType();
        type.setName(typeName);
        return trainingTypeRepository.save(type);
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
        return traineeRepository.save(t);
    }

    private Training persistTraining(String name, Trainer trainer, Trainee trainee,
                                     TrainingType type, LocalDate date, int duration) {
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
