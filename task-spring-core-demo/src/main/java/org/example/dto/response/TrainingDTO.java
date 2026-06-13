package org.example.dto.response;

import java.time.LocalDate;

public record TrainingDTO(
        Long id,
        Long traineeId,
        Long trainerId,
        String name,
        Integer typeId,
        LocalDate date,
        int duration) {
}
