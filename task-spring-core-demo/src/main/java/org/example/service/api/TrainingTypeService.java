package org.example.service.api;

import org.example.dto.response.TrainingTypeDTO;
import org.example.entity.TrainingType;

import java.util.List;

public interface TrainingTypeService {

    TrainingTypeDTO findById(Integer id);
    List<TrainingTypeDTO> findAll();
    boolean existsById(Integer id);
    TrainingType getReferenceById(Integer id);
}
