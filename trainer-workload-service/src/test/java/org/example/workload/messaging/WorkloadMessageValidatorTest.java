package org.example.workload.messaging;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.ActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkloadMessageValidator Unit Tests")
class WorkloadMessageValidatorTest {

    private WorkloadMessageValidator validator;

    @BeforeEach
    void setUp() {
        Validator jakartaValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new WorkloadMessageValidator(jakartaValidator);
    }

    @Test
    @DisplayName("isValid returns true for a fully valid request")
    void isValid_ReturnsTrue_ForValidRequest() {
        assertThat(validator.isValid(validRequest())).isTrue();
    }

    @Test
    @DisplayName("isValid returns false when trainerUsername is blank")
    void isValid_ReturnsFalse_WhenUsernameIsBlank() {
        WorkloadEventRequest request = new WorkloadEventRequest(
                "",
                "John",
                "Smith",
                true,
                LocalDate.of(2026, 6, 15),
                60,
                ActionType.ADD
        );
        assertThat(validator.isValid(request)).isFalse();
    }

    @Test
    @DisplayName("isValid returns false when trainingDate is null")
    void isValid_ReturnsFalse_WhenTrainingDateIsNull() {
        WorkloadEventRequest request = new WorkloadEventRequest(
                "trainer.one",
                "John",
                "Smith",
                true,
                null,
                60,
                ActionType.ADD
        );
        assertThat(validator.isValid(request)).isFalse();
    }

    @Test
    @DisplayName("isValid returns false when trainingDuration is zero (violates @Positive)")
    void isValid_ReturnsFalse_WhenDurationIsZero() {
        WorkloadEventRequest request = new WorkloadEventRequest(
                "trainer.one",
                "John",
                "Smith",
                true,
                LocalDate.of(2026, 6, 15),
                0,
                ActionType.ADD
        );
        assertThat(validator.isValid(request)).isFalse();
    }

    @Test
    @DisplayName("isValid returns false when actionType is null")
    void isValid_ReturnsFalse_WhenActionTypeIsNull() {
        WorkloadEventRequest request = new WorkloadEventRequest(
                "trainer.one",
                "John",
                "Smith",
                true,
                LocalDate.of(2026, 6, 15),
                60,
                null
        );
        assertThat(validator.isValid(request)).isFalse();
    }

    @Test
    @DisplayName("validationErrors returns constraint message for blank username")
    void validationErrors_ReturnsConstraintMessage_ForBlankUsername() {
        WorkloadEventRequest request = new WorkloadEventRequest(
                "",
                "John",
                "Smith",
                true,
                LocalDate.of(2026, 6, 15),
                60,
                ActionType.ADD
        );
        String errors = validator.validationErrors(request);
        assertThat(errors).isNotBlank();
    }

    @Test
    @DisplayName("validationErrors returns all constraint messages for multiple violations")
    void validationErrors_ReturnsAllMessages_ForMultipleViolations() {
        WorkloadEventRequest request = new WorkloadEventRequest(
                "",
                "",
                "",
                true,
                null,
                0,
                null
        );
        String errors = validator.validationErrors(request);
        // Multiple violations; just assert there is content — constraint messages are implementation-specific
        assertThat(errors).isNotBlank();
        assertThat(errors).contains(",");
    }

    private WorkloadEventRequest validRequest() {
        return new WorkloadEventRequest(
                "trainer.one",
                "John",
                "Smith",
                true,
                LocalDate.of(2026, 6, 15),
                60,
                ActionType.ADD
        );
    }
}