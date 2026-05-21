package org.example.dto.request;

import java.time.LocalDate;

public record TrainingsOfTraineeSearchCriteria(
        String traineeUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        Integer typeId
) {
}
