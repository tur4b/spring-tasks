package org.example.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrainerUpdateRequest(

        @NotBlank(message = "Username can't be blank")
        String username,

        @NotBlank(message = "First name can't be blank")
        @Size(min = 3, max = 30, message = "Size of first name min=3, max=30")
        String firstName,

        @NotBlank(message = "Last name can't be blank")
        @Size(min = 3, max = 60, message = "Size of last name min=3, max=60")
        String lastName,

        @NotNull(message = "Specialization id can't be null")
        Integer specializationId
) {
}
