package org.example.mapper;

import org.example.dto.request.TraineeCreateRequest;
import org.example.dto.response.TraineeDTO;
import org.example.entity.Trainee;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper {

    public TraineeDTO toDTO(Trainee trainee, Long userId) {
        return new TraineeDTO(
                trainee.getId(),
                userId,
                trainee.getAddress(),
                trainee.isActive(),
                trainee.getDateOfBirth(),
                trainee.getCreatedAt(),
                trainee.getUpdatedAt()
        );
    }

    public Trainee toEntity(TraineeCreateRequest traineeCreateRequest) {
        Trainee trainee = new Trainee();
        trainee.setAddress(traineeCreateRequest.address());
        trainee.setDateOfBirth(traineeCreateRequest.dateOfBirth());
        return trainee;
    }

}
