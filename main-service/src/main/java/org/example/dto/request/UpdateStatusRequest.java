package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotBlank(message = "Username can't be blank")
        String username,
        @NotNull(message = "Status can't be null")
        Boolean active
) {
}
