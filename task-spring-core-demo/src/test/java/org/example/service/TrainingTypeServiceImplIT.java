package org.example.service;

import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.dao.TrainingTypeRepository;
import org.example.dto.response.TrainingTypeDTO;
import org.example.service.api.TrainingTypeService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TrainingTypeService - Service Slice Integration Tests")
class TrainingTypeServiceImplIT extends AbstractServiceSliceTest {

    @Autowired
    private TrainingTypeService trainingTypeService;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DisplayName("findById - returns DTO for saved type")
    void findById_ReturnsDTOForSavedType() {
        TrainingType type = new TrainingType();
        type.setName(TrainingTypeName.CARDIO);
        TrainingType saved = trainingTypeRepository.save(type);

        TrainingTypeDTO dto = trainingTypeService.findById(saved.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.name()).isEqualTo(TrainingTypeName.CARDIO);
    }

    @Test
    @DisplayName("findById - throws EntityNotFoundException for missing id")
    void findById_Missing_Throws() {
        assertThatThrownBy(() -> trainingTypeService.findById(99999))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findAll - includes all persisted types")
    void findAll_IncludesPersistedTypes() {
        TrainingType t = new TrainingType();
        t.setName(TrainingTypeName.STRENGTH);
        trainingTypeRepository.save(t);

        assertThat(trainingTypeService.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("existsById - correct booleans")
    void existsById() {
        TrainingType type = new TrainingType();
        type.setName(TrainingTypeName.CARDIO);
        TrainingType saved = trainingTypeRepository.save(type);

        assertThat(trainingTypeService.existsById(saved.getId())).isTrue();
        assertThat(trainingTypeService.existsById(99999)).isFalse();
    }
}

