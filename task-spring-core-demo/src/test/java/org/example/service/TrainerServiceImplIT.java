package org.example.service;

import org.example.dao.TrainerRepository;
import org.example.dao.TrainingTypeRepository;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.request.TrainerUpdateRequest;
import org.example.dto.request.UpdateStatusRequest;
import org.example.dto.response.UserCredentialsDTO;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.service.api.TrainerService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrainerService - Service Slice Integration Tests")
class TrainerServiceImplIT extends AbstractServiceSliceTest {

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    // ─── createTrainer ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTrainer - creates trainer with specialization and returns credentials")
    void createTrainer_ValidRequest_ReturnsCredentials() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.CARDIO);
        TrainerCreateRequest request = new TrainerCreateRequest(
                "New", "Trainer", specialization.getId());

        UserCredentialsDTO result = trainerService.createTrainer(request);

        assertThat(result.id()).isNotNull();
        assertThat(result.username()).isNotBlank();
        assertThat(result.password()).isNotBlank();
        // Verify trainer exists
        assertThat(trainerRepository.existsByUserUsername(result.username())).isTrue();
    }

    @Test
    @DisplayName("createTrainer - throws NotFoundException when specialization does not exist")
    void createTrainer_InvalidSpecialization_ThrowsNotFoundException() {
        TrainerCreateRequest request = new TrainerCreateRequest("New", "Trainer", 99999);

        assertThatThrownBy(() -> trainerService.createTrainer(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("TrainingType not found");
    }

    // ─── findTrainerViewByUsername ─────────────────────────────────────────────

    @Test
    @DisplayName("findTrainerViewByUsername - returns profile view for existing trainer")
    void findTrainerViewByUsername_ExistingTrainer_ReturnsView() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.STRENGTH);
        persistTrainer("profile.trainer", specialization);

        var result = trainerService.findTrainerViewByUsername("profile.trainer");

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isNotBlank();
        assertThat(result.specializationId()).isEqualTo(specialization.getId());
    }

    @Test
    @DisplayName("findTrainerViewByUsername - throws NotFoundException for non-existent trainer")
    void findTrainerViewByUsername_NonExistent_ThrowsNotFoundException() {
        assertThatThrownBy(() -> trainerService.findTrainerViewByUsername("ghost.trainer"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer not found");
    }

    // ─── updateTrainer ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTrainer - updates trainer and specialization")
    void updateTrainer_ValidUpdate_ReturnsUpdatedView() {
        TrainingType oldSpec = persistSpecialization(TrainingTypeName.STRENGTH);
        TrainingType newSpec = persistSpecialization(TrainingTypeName.CARDIO);
        persistTrainer("update.trainer", oldSpec);

        var request = new TrainerUpdateRequest(
                "update.trainer", "Updated", "Name", newSpec.getId());
        var result = trainerService.updateTrainer(request);

        assertThat(result.firstName()).isEqualTo("Updated");
        assertThat(result.lastName()).isEqualTo("Name");
        assertThat(result.specializationId()).isEqualTo(newSpec.getId());
    }

    @Test
    @DisplayName("updateTrainer - throws NotFoundException for non-existent trainer")
    void updateTrainer_NonExistent_ThrowsNotFoundException() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.CARDIO);
        var request = new TrainerUpdateRequest(
                "ghost.trainer", "First", "Last", specialization.getId());

        assertThatThrownBy(() -> trainerService.updateTrainer(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer not found");
    }

    @Test
    @DisplayName("updateTrainer - throws NotFoundException when specialization does not exist")
    void updateTrainer_InvalidSpecialization_ThrowsNotFoundException() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.CARDIO);
        persistTrainer("update.trainer2", specialization);

        var request = new TrainerUpdateRequest(
                "update.trainer2", "First", "Last", 99999);

        assertThatThrownBy(() -> trainerService.updateTrainer(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("TrainingType not found");
    }

    // ─── deleteTrainer ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTrainer - deletes trainer and returns true")
    void deleteTrainer_ExistingTrainer_ReturnsTrue() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.CARDIO);
        persistTrainer("delete.trainer", specialization);

        boolean result = trainerService.deleteTrainer("delete.trainer");

        assertThat(result).isTrue();
        assertThat(trainerRepository.existsByUserUsername("delete.trainer")).isFalse();
    }

    @Test
    @DisplayName("deleteTrainer - returns false for non-existent trainer")
    void deleteTrainer_NonExistent_ReturnsFalse() {
        assertThat(trainerService.deleteTrainer("ghost.trainer")).isFalse();
    }

    // ─── existsById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("existsById - returns true for existing trainer")
    void existsById_ExistingId_ReturnsTrue() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.CARDIO);
        Trainer trainer = persistTrainer("exists.check", specialization);

        assertThat(trainerService.existsById(trainer.getId())).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false for non-existent id")
    void existsById_NonExistent_ReturnsFalse() {
        assertThat(trainerService.existsById(99999L)).isFalse();
    }

    // ─── findTrainerByUsername ────────────────────────────────────────────────

    @Test
    @DisplayName("findTrainerByUsername - returns trainer entity for existing username")
    void findTrainerByUsername_ExistingUsername_ReturnsEntity() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.STRENGTH);
        persistTrainer("find.entity", specialization);

        Trainer result = trainerService.findTrainerByUsername("find.entity");

        assertThat(result).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo("find.entity");
    }

    @Test
    @DisplayName("findTrainerByUsername - throws NotFoundException for non-existent username")
    void findTrainerByUsername_NonExistent_ThrowsNotFoundException() {
        assertThatThrownBy(() -> trainerService.findTrainerByUsername("ghost.trainer"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer not found");
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus(active=true) - activates inactive trainer")
    void updateStatus_Activate_Success() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.CARDIO);
        Trainer trainer = persistTrainer("activate.trainer", specialization);
        trainer.setActive(false);
        trainerRepository.save(trainer);

        trainerService.updateStatus(new UpdateStatusRequest("activate.trainer", true));

        assertThat(trainerRepository.findByUserUsername("activate.trainer"))
                .isPresent()
                .get()
                .extracting(Trainer::isActive)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("updateStatus(active=false) - deactivates active trainer")
    void updateStatus_Deactivate_Success() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.CARDIO);
        Trainer trainer = persistTrainer("deactivate.trainer", specialization);
        trainer.setActive(true);
        trainerRepository.save(trainer);

        trainerService.updateStatus(new UpdateStatusRequest("deactivate.trainer", false));

        assertThat(trainerRepository.findByUserUsername("deactivate.trainer"))
                .isPresent()
                .get()
                .extracting(Trainer::isActive)
                .isEqualTo(false);
    }

    @Test
    @DisplayName("updateStatus - throws RuntimeException when already active and activating")
    void updateStatus_AlreadyActive_ThrowsException() {
        TrainingType specialization = persistSpecialization(TrainingTypeName.CARDIO);
        Trainer trainer = persistTrainer("active.trainer", specialization);
        trainer.setActive(true);
        trainerRepository.save(trainer);

        assertThatThrownBy(() ->
                trainerService.updateStatus(new UpdateStatusRequest("active.trainer", true)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already active");
    }

    // ─── helper ───────────────────────────────────────────────────────────────

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

    private TrainingType persistSpecialization(TrainingTypeName name) {
        TrainingType type = new TrainingType();
        type.setName(name);
        return trainingTypeRepository.save(type);
    }
}

