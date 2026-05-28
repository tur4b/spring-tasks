package org.example.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record TrainerCreateRequest(
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
