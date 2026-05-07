package org.example.dto.response;

import java.time.LocalDateTime;

public record UserDTO(
        Long id,
        String firstName,
        String lastName,
        String username,
        LocalDateTime createdAt
) {
}
