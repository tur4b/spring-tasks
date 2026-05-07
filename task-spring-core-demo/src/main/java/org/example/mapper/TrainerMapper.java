package org.example.mapper;

import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.response.TrainerDTO;
import org.example.entity.Trainer;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper {

    public TrainerDTO toDTO(Trainer trainer) {
        return new TrainerDTO(
                trainer.getId(),
                trainer.getUserId(),
                trainer.getSpecialization(),
                trainer.getCreatedAt()
        );
    }

    public Trainer toEntity(TrainerCreateRequest createRequest) {
        Trainer trainer = new Trainer();
        trainer.setSpecialization(createRequest.specialization());
        return trainer;
    }
}
