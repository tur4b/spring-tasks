package org.example.service;

import org.example.dao.TraineeRepository;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.request.TraineeUpdateRequest;
import org.example.dto.request.UpdateStatusRequest;
import org.example.dto.response.UserCredentialsDTO;
import org.example.entity.Trainee;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.service.api.TraineeService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TraineeService - Service Slice Integration Tests")
class TraineeServiceImplIT extends AbstractServiceSliceTest {

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TraineeRepository traineeRepository;


    // ─── createTrainee ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTrainee - creates trainee with user and returns credentials")
    void createTrainee_Success_ReturnsCredentials() {
        TraineeCreateRequest request = new TraineeCreateRequest(
                "New", "Trainee", "Baku", LocalDate.of(2000, 1, 1));

        UserCredentialsDTO result = traineeService.createTrainee(request);

        assertThat(result.id()).isNotNull();
        assertThat(result.username()).isNotBlank();
        assertThat(result.password()).isNotBlank();
        // Verify trainee exists
        assertThat(traineeRepository.existsByUserUsername(result.username())).isTrue();
    }

    // ─── findTraineeViewByUsername ─────────────────────────────────────────────

    @Test
    @DisplayName("findTraineeViewByUsername - returns profile view for existing trainee")
    void findTraineeViewByUsername_ExistingTrainee_ReturnsView() {
        persistTrainee("profile.trainee", LocalDate.of(1999, 5, 15), "Home");

        var result = traineeService.findTraineeViewByUsername("profile.trainee");

        assertThat(result).isNotNull();
        assertThat(result.firstName()).isNotBlank();
        assertThat(result.address()).isEqualTo("Home");
    }

    @Test
    @DisplayName("findTraineeViewByUsername - throws NotFoundException for non-existent trainee")
    void findTraineeViewByUsername_NonExistent_ThrowsNotFoundException() {
        assertThatThrownBy(() -> traineeService.findTraineeViewByUsername("ghost.trainee"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found");
    }

    // ─── updateTrainee ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTrainee - updates trainee details and returns view")
    void updateTrainee_ValidUpdate_ReturnsUpdatedView() {
        persistTrainee("update.trainee", LocalDate.of(1990, 1, 1), "OldAddress");

        var request = new TraineeUpdateRequest(
                "update.trainee", "Updated", "Name", "NewAddress", LocalDate.of(1995, 6, 1));
        var result = traineeService.updateTrainee(request);

        assertThat(result.firstName()).isEqualTo("Updated");
        assertThat(result.lastName()).isEqualTo("Name");
        assertThat(result.address()).isEqualTo("NewAddress");
    }

    @Test
    @DisplayName("updateTrainee - throws NotFoundException for non-existent trainee")
    void updateTrainee_NonExistent_ThrowsNotFoundException() {
        var request = new TraineeUpdateRequest(
                "ghost.trainee", "First", "Last", "Address", LocalDate.now());

        assertThatThrownBy(() -> traineeService.updateTrainee(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found");
    }

    // ─── deleteTrainee ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTrainee - deletes trainee and returns true")
    void deleteTrainee_ExistingTrainee_ReturnsTrue() {
        persistTrainee("delete.trainee", LocalDate.of(1998, 1, 1), "Baku");

        boolean result = traineeService.deleteTrainee("delete.trainee");

        assertThat(result).isTrue();
        assertThat(traineeRepository.existsByUserUsername("delete.trainee")).isFalse();
    }

    @Test
    @DisplayName("deleteTrainee - returns false for non-existent trainee")
    void deleteTrainee_NonExistent_ReturnsFalse() {
        assertThat(traineeService.deleteTrainee("ghost.trainee")).isFalse();
    }

    // ─── existsByUsername ──────────────────────────────────────────────────────

    @Test
    @DisplayName("existsByUsername - returns true for existing trainee")
    void existsByUsername_ExistingTrainee_ReturnsTrue() {
        persistTrainee("exists.check", LocalDate.of(2000, 1, 1), "City");

        assertThat(traineeService.existsByUsername("exists.check")).isTrue();
    }

    @Test
    @DisplayName("existsByUsername - returns false for non-existent trainee")
    void existsByUsername_NonExistent_ReturnsFalse() {
        assertThat(traineeService.existsByUsername("ghost.trainee")).isFalse();
    }

    // ─── findTraineeByUsername ────────────────────────────────────────────────

    @Test
    @DisplayName("findTraineeByUsername - returns trainee entity for existing username")
    void findTraineeByUsername_ExistingUsername_ReturnsEntity() {
        persistTrainee("find.entity", LocalDate.of(2000, 1, 1), "Baku");

        Trainee result = traineeService.findTraineeByUsername("find.entity");

        assertThat(result).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo("find.entity");
    }

    @Test
    @DisplayName("findTraineeByUsername - throws NotFoundException for non-existent username")
    void findTraineeByUsername_NonExistent_ThrowsNotFoundException() {
        assertThatThrownBy(() -> traineeService.findTraineeByUsername("ghost.trainee"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found");
    }

    // ─── existsById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("existsById - returns true for existing trainee")
    void existsById_ExistingId_ReturnsTrue() {
        Trainee trainee = persistTrainee("id.check", LocalDate.of(2000, 1, 1), "Baku");

        assertThat(traineeService.existsById(trainee.getId())).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false for non-existent id")
    void existsById_NonExistent_ReturnsFalse() {
        assertThat(traineeService.existsById(99999L)).isFalse();
    }

    // ─── getReferenceById ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getReferenceById - returns trainee reference")
    void getReferenceById_ExistingId_ReturnsReference() {
        Trainee trainee = persistTrainee("ref.trainee", LocalDate.of(2000, 1, 1), "Baku");

        Trainee result = traineeService.getReferenceById(trainee.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(trainee.getId());
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus(active=true) - activates inactive trainee")
    void updateStatus_Activate_Success() {
        Trainee trainee = persistTrainee("activate.trainee", LocalDate.of(2000, 1, 1), "Baku");
        trainee.setActive(false);
        traineeRepository.save(trainee);

        traineeService.updateStatus(new UpdateStatusRequest("activate.trainee", true));

        assertThat(traineeRepository.findByUserUsername("activate.trainee"))
                .isPresent()
                .get()
                .extracting(Trainee::isActive)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("updateStatus(active=false) - deactivates active trainee")
    void updateStatus_Deactivate_Success() {
        Trainee trainee = persistTrainee("deactivate.trainee", LocalDate.of(2000, 1, 1), "Baku");
        trainee.setActive(true);
        traineeRepository.save(trainee);

        traineeService.updateStatus(new UpdateStatusRequest("deactivate.trainee", false));

        assertThat(traineeRepository.findByUserUsername("deactivate.trainee"))
                .isPresent()
                .get()
                .extracting(Trainee::isActive)
                .isEqualTo(false);
    }

    @Test
    @DisplayName("updateStatus - throws RuntimeException when already active and activating")
    void updateStatus_AlreadyActive_ThrowsException() {
        Trainee trainee = persistTrainee("active.trainee", LocalDate.of(2000, 1, 1), "Baku");
        trainee.setActive(true);
        traineeRepository.save(trainee);

        assertThatThrownBy(() ->
                traineeService.updateStatus(new UpdateStatusRequest("active.trainee", true)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already active");
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private Trainee persistTrainee(String username, LocalDate dob, String address) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName("Trainee");
        user.setLastName("User");
        user.setPassword("encoded");
        user.setActive(true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(dob);
        trainee.setAddress(address);
        trainee.setActive(true);

        return traineeRepository.save(trainee);
    }
}

