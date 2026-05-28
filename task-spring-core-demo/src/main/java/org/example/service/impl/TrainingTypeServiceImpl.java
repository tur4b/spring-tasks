package org.example.service.impl;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dao.TrainingTypeRepository;
import org.example.dto.response.TrainingTypeDTO;
import org.example.entity.TrainingType;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.mapper.TrainingTypeMapper;
import org.example.service.api.TrainingTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingTypeMapper trainingTypeMapper;

    /**
     * Get TrainingTypeDTO by training id
     *
     * @param id the id of TrainingType
     * @return TrainingTypeDTO
     */
    @Override
    public TrainingTypeDTO findById(Integer id) {
        return trainingTypeRepository.findById(id)
                .map(trainingTypeMapper::toDTO)
                .orElseThrow(() -> new NotFoundException("TrainingType found with ID: " + id, ErrorResponse.ErrorPointer.id));
    }

    /**
     * Get the list of TrainingTypeDTO
     *
     * @return List<TrainingTypeDTO> the list of TrainingTypeDTO
     */
    @Override
    public List<TrainingTypeDTO> findAll() {
        return trainingTypeRepository.findAll()
                .stream()
                .map(trainingTypeMapper::toDTO)
                .toList();
    }

    /**
     * Check if TrainingType exists by training id
     *
     * @param id the id of the TrainingType
     * @return boolean true if TrainingType exists by id, false if it does not exist
     */
    @Override
    public boolean existsById(Integer id) {
        return trainingTypeRepository.existsById(id);
    }

    /**
     * Get TrainingType reference
     *
     * @param id the id of TrainingType
     * @return TrainingType reference to TrainingType entity
     */
    @Override
    public TrainingType getReferenceById(Integer id) {
        return trainingTypeRepository.getReferenceById(id);
    }
}
