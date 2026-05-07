package org.example.dto.request;

import java.time.LocalDate;

public record TraineeUpdateRequest(
        String lastName,
        String firstName,
        String address,
        LocalDate dateOfBirth
) {
}
