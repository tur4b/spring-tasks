package org.example.mapper;

import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.response.TrainerDTO;
import org.example.entity.Trainer;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper {

    public TrainerDTO toDTO(Trainer trainer, Long userId, Integer specializationId) {
        return new TrainerDTO(
                trainer.getId(),
                userId,
                specializationId,
                trainer.isActive(),
                trainer.getCreatedAt(),
                trainer.getUpdatedAt()
        );
    }
}
