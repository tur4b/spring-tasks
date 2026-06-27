package org.example.service.api;

import org.example.dto.response.TrainingTypeDTO;
import org.example.entity.TrainingType;

import java.util.List;

public interface TrainingTypeService {

    /**
     * Retrieve a training type DTO by its ID.
     *
     * @param id the training type ID
     * @return TrainingTypeDTO
     * @throws org.example.exception.model.NotFoundException if no type with the given ID exists
     */
    TrainingTypeDTO findById(Integer id);

    /**
     * Retrieve all available training types.
     *
     * @return list of TrainingTypeDTOs
     */
    List<TrainingTypeDTO> findAll();

    /**
     * Check whether a training type exists by ID.
     *
     * @param id the training type ID
     * @return {@code true} if a type with the given ID exists
     */
    boolean existsById(Integer id);

    /**
     * Return a JPA reference (proxy) to a TrainingType entity without loading it.
     *
     * @param id the training type ID
     * @return TrainingType proxy reference
     */
    TrainingType getReferenceById(Integer id);
}
