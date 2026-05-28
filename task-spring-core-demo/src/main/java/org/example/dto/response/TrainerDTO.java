package org.example.dto.response;

public record TrainerDTO(
        Long id,
        String firstName,
        String lastName,
        Integer specializationId,
        boolean isActive
) {
}
