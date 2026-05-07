package org.example.dto.response;

import org.example.entity.TrainingType;

import java.time.LocalDateTime;

public record TrainerDTO(
        Long id,
        Long userId,
        TrainingType specialization,
        LocalDateTime createdAt
) {
}
