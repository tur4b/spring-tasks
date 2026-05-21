package org.example.dto.response;

import java.time.LocalDateTime;

public record TrainerDTO(
        Long id,
        Long userId,
        Integer specializationId,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
