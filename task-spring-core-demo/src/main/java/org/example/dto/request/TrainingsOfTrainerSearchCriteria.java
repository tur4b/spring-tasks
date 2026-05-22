package org.example.dto.request;

import java.time.LocalDate;

public record TrainingsOfTrainerSearchCriteria(
        String trainerUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName,
        Integer typeId
) {
}
