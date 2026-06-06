package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TraineeCreateRequest(
        @NotBlank(message = "First name can't be blank")
        @Size(min = 3, max = 30, message = "Size of first name min=3, max=30")
        String firstName,

        @NotBlank(message = "Last name can't be blank")
        @Size(min = 3, max = 60, message = "Size of last name min=3, max=60")
        String lastName,

        String address,
        LocalDate dateOfBirth
) {
}
