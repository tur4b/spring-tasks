package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TraineeUpdateRequest(
        @NotBlank(message = "Last name can't be blank")
        @Size(min = 3, max = 60, message = "Size of last name min=3, max=60")
        String lastName,

        @NotBlank(message = "First name can't be blank")
        @Size(min = 3, max = 30, message = "Size of first name min=3, max=30")
        String firstName,

        String address,
        LocalDate dateOfBirth
) {
}
