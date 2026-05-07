package org.example.dto.response;

import org.example.entity.TrainingType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TrainingDTO(
        Long id,
        Long traineeId,
        Long trainerId,
        String name,
        TrainingType type,
        LocalDate date,
        int duration,
        LocalDateTime createdAt
) {
}
