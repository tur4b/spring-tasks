package org.example.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record UserCredentialsDTO(
        @JsonIgnore Long id,
        String username,
        String password
) {
}
