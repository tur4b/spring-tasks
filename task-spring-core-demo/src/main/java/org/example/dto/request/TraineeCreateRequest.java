package org.example.dto.request;

import java.time.LocalDate;

public record TraineeCreateRequest(
        String firstName,
        String lastName,
        String address,
        LocalDate dateOfBirth
) {
}
