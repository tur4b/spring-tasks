package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Username can't be blank")
        @Schema(
                description = "Username of the user",
                example = "trainee.admin"
        )
        String username,
        @NotBlank(message = "Password can't be blank")
        @Schema(
                description = "Password of the user",
                example = "admin123"
        )
        String password) {

}
