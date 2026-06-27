package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank(message = "First name can't be blank") String firstName,
        @NotBlank(message = "Last name can't be blank") String lastName
) {
}
