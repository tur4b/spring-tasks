package org.example.dto.request;

import javax.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TrainingsOfTraineeSearchCriteria(
        @NotBlank(message = "Trainee username can't be blank")
        String traineeUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        Integer typeId
) {
}
