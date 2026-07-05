package org.example.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record WorkloadEntryRequest(

        @NotBlank(message = "Trainer first name can't be blank")
        String trainerFirstName,

        @NotBlank(message = "Trainer last name can't be blank")
        String trainerLastName,

        boolean isActive,

        @NotNull(message = "Training date can't be null")
        LocalDate trainingDate,

        @Positive(message = "Training duration should be positive")
        int trainingDuration

) {
}
