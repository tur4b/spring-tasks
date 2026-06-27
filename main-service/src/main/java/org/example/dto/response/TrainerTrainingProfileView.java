package org.example.dto.response;

import java.time.LocalDate;

public record TrainerTrainingProfileView(
        String name,
        LocalDate date,
        Integer typeId,
        int duration,
        String traineeName
) {
}
