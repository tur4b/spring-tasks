package org.example.service;

import org.example.dao.TraineeRepository;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.response.TraineeDTO;
import org.example.entity.Trainee;
import org.example.entity.User;
import org.example.service.api.PasswordEncoder;
import org.example.service.api.TraineeService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TraineeService - Service Slice Integration Tests")
class TraineeServiceImplIT extends AbstractServiceSliceTest {

    private static final AuthRequest AUTHENTICATED_USER = new AuthRequest("it.auth.user", "it-pass");
    private static final AuthRequest ANONYMOUS_USER = new AuthRequest("anonymous.user", "anonymous-pass");

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void ensureAuthenticatedUserExists() {
        userRepository.findByUsername(AUTHENTICATED_USER.username())
                .orElseGet(() -> userRepository.save(user(AUTHENTICATED_USER.username(), AUTHENTICATED_USER.password())));
    }

    @Test
    @DisplayName("createTrainee - persists trainee + linked user")
    void createTrainee_PersistsTraineeAndUser() {
        TraineeDTO dto = traineeService.createTrainee(
                new TraineeCreateRequest("John", "Doe", "Baku", LocalDate.of(2000, 1, 1)));

        assertThat(dto).isNotNull();
        assertThat(traineeRepository.existsByUserUsername("john.doe")).isTrue();
        assertThat(userRepository.findByUsername("john.doe")).isPresent();
    }

    @Test
    @DisplayName("existsById - true for saved trainee")
    void existsById_TrueForSavedTrainee() {
        TraineeDTO dto = traineeService.createTrainee(
                new TraineeCreateRequest("Jane", "Roe", "London", LocalDate.of(1999, 5, 5)));

        assertThat(traineeService.existsById(dto.id())).isTrue();
    }

    @Test
    @DisplayName("deleteTraineeByUsername - removes trainee, returns true; repeat returns false")
    void deleteTraineeByUsername() {
        traineeService.createTrainee(
                new TraineeCreateRequest("Del", "Me", "Paris", LocalDate.of(1995, 3, 3)));

        assertThat(traineeService.deleteTraineeByUsername("del.me", AUTHENTICATED_USER)).isTrue();
        assertThat(traineeService.deleteTraineeByUsername("del.me", AUTHENTICATED_USER)).isFalse();
    }

    @Test
    @DisplayName("changePassword - throws EntityNotFoundException for unknown trainee")
    void changePassword_UnknownTrainee_Throws() {
        assertThatThrownBy(() ->
                traineeService.changePassword(
                        new ChangePasswordRequest("nobody", "pw"), AUTHENTICATED_USER))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("deactivate - should set active=false and reject second deactivate call")
    void deactivate_ShouldSetInactiveAndRejectDuplicateDeactivate() {
        traineeService.createTrainee(new TraineeCreateRequest("Toggle", "State", "Baku", LocalDate.of(2002, 4, 4)));
        Long traineeId = traineeRepository.findByUserUsername("toggle.state").orElseThrow().getId();

        traineeService.deactivate(traineeId, AUTHENTICATED_USER);

        assertThat(traineeRepository.findById(traineeId))
                .isPresent()
                .get()
                .extracting(Trainee::isActive)
                .isEqualTo(false);

        assertThatThrownBy(() -> traineeService.deactivate(traineeId, AUTHENTICATED_USER))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainee already deactive");
    }

    @Test
    @DisplayName("activate - should set active=true when trainee is currently inactive")
    void activate_ShouldSetActive_WhenTraineeIsInactive() {
        traineeService.createTrainee(new TraineeCreateRequest("Activate", "Me", "Baku", LocalDate.of(2002, 4, 4)));
        Long traineeId = traineeRepository.findByUserUsername("activate.me").orElseThrow().getId();

        traineeService.deactivate(traineeId, AUTHENTICATED_USER);
        traineeService.activate(traineeId, AUTHENTICATED_USER);

        assertThat(traineeRepository.findById(traineeId))
                .isPresent()
                .get()
                .extracting(Trainee::isActive)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("deactivate - throws SecurityException when anonymous credentials are provided")
    void deactivate_ShouldThrowSecurityException_WhenAnonymousCredentialsAreUsed() {
        traineeService.createTrainee(new TraineeCreateRequest("Anon", "Case", "Baku", LocalDate.of(2002, 4, 4)));
        Long traineeId = traineeRepository.findByUserUsername("anon.case").orElseThrow().getId();

        assertThatThrownBy(() -> traineeService.deactivate(traineeId, ANONYMOUS_USER))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("INvalid credentials");
    }

    private User user(String username, String password) {
        User user = new User();
        user.setFirstName("IT");
        user.setLastName("Auth");
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }
}

