package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Username can't be blnk") String username,
        @NotBlank(message = "Password can't be blnk") String password) {
}
