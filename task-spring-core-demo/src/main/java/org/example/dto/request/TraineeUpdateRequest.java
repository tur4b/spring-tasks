package org.example.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import java.time.LocalDate;

public record TraineeUpdateRequest(

        @NotBlank(message = "Username can't be blank")
        String username,

        @NotBlank(message = "First name can't be blank")
        @Size(min = 3, max = 30, message = "Size of first name min=3, max=30")
        String firstName,

        @NotBlank(message = "Last name can't be blank")
        @Size(min = 3, max = 60, message = "Size of last name min=3, max=60")
        String lastName,

        String address,

        @Past(message = "Birthdate should be past")
        LocalDate dateOfBirth
) {
}
