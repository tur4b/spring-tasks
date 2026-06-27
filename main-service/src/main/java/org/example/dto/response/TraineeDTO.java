package org.example.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TraineeDTO(
        Long id,
        String firstName,
        String lastName,
        String address,
        LocalDate dateOfBirth,
        boolean isActive
) {
}
