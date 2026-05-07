package org.example.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TraineeDTO(
        Long id,
        Long userId,
        String address,
        LocalDate dateOfBirth,
        LocalDateTime createdAt
) {
}
