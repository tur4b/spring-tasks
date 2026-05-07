package org.example.dto.request;

import org.example.entity.TrainingType;

import java.time.LocalDate;

public record TrainingCreateRequest(
        Long traineeId,
        Long trainerId,
        String name,
        TrainingType type,
        LocalDate date,
        int duration
) {
}
