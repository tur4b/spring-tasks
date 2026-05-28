package org.example.dto.response;

import java.time.LocalDate;
import java.util.List;

public record TraineeProfileView(
        String firstName,
        String lastName,
        String address,
        LocalDate dateOfBirth,
        boolean isActive,
        List<TraineeProfileTrainerDTO> trainers
) {
}
