package org.example.dto.request;

import javax.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TrainingsOfTrainerSearchCriteria(
        @NotBlank(message = "Trainer username can't be blank")
        String trainerUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName,
        Integer typeId
) {
}
