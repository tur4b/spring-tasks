package org.example.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TrainingCreateRequest(

        @NotBlank(message = "Trainee username can't be blank")
        String traineeUsername,

        @NotBlank(message = "Trainer username can't be blank")
        String trainerUsername,

        @NotBlank(message = "Name can't be blank")
        String name,

        @NotNull(message = "Date can't be null")
        @FutureOrPresent(message = "Training date should be future or present")
        LocalDate date,

        @Positive(message = "Duration should me positive value")
        int duration

) {
}
