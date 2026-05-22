package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Username can't be blank") String username,
        @NotBlank(message = "Password can't be blank") String password
) {
}
