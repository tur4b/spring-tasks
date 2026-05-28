package org.example.dto.response;

import java.time.LocalDate;

public record TraineeTrainingProfileView(
        String name,
        LocalDate date,
        Integer typeId,
        int duration,
        String trainerName
) {
}
