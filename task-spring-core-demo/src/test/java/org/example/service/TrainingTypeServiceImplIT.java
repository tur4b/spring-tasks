package org.example.service;

import org.example.dao.TrainingTypeRepository;
import org.example.dto.response.TrainingTypeDTO;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.exception.model.NotFoundException;
import org.example.service.api.TrainingTypeService;
import org.example.testsupport.AbstractServiceSliceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrainingTypeService - Service Slice Integration Tests")
class TrainingTypeServiceImplIT extends AbstractServiceSliceTest {

    @Autowired
    private TrainingTypeService trainingTypeService;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    // ─── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById - returns TrainingTypeDTO for existing type")
    void findById_ExistingType_ReturnsDTO() {
        TrainingType saved = persistTrainingType(TrainingTypeName.CARDIO);

        TrainingTypeDTO result = trainingTypeService.findById(saved.getId());

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.name()).isEqualTo(TrainingTypeName.CARDIO);
    }

    @Test
    @DisplayName("findById - throws NotFoundException for non-existent type")
    void findById_NonExistent_ThrowsNotFoundException() {
        assertThatThrownBy(() -> trainingTypeService.findById(99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("TrainingType found with ID");
    }

    // ─── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll - returns all training types")
    void findAll_ReturnsAllTypes() {
        persistTrainingType(TrainingTypeName.CARDIO);
        persistTrainingType(TrainingTypeName.STRENGTH);

        List<TrainingTypeDTO> result = trainingTypeService.findAll();

        assertThat(result).isNotEmpty().hasSizeGreaterThanOrEqualTo(2);
        assertThat(result.stream().map(TrainingTypeDTO::name))
                .contains(TrainingTypeName.CARDIO, TrainingTypeName.STRENGTH);
    }

    @Test
    @DisplayName("findAll - returns complete list when multiple types exist")
    void findAll_MultipleTypes_ReturnsAll() {
        TrainingType type1 = persistTrainingType(TrainingTypeName.CARDIO);
        TrainingType type2 = persistTrainingType(TrainingTypeName.STRENGTH);

        List<TrainingTypeDTO> result = trainingTypeService.findAll();

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result.stream().map(TrainingTypeDTO::id))
                .contains(type1.getId(), type2.getId());
    }

    // ─── existsById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("existsById - returns true for existing type")
    void existsById_ExistingId_ReturnsTrue() {
        TrainingType saved = persistTrainingType(TrainingTypeName.STRENGTH);

        assertThat(trainingTypeService.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false for non-existent type")
    void existsById_NonExistent_ReturnsFalse() {
        assertThat(trainingTypeService.existsById(99999)).isFalse();
    }

    // ─── getReferenceById ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getReferenceById - returns reference to existing type")
    void getReferenceById_ExistingId_ReturnsReference() {
        TrainingType saved = persistTrainingType(TrainingTypeName.STRENGTH);

        TrainingType result = trainingTypeService.getReferenceById(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private TrainingType persistTrainingType(TrainingTypeName name) {
        TrainingType type = new TrainingType();
        type.setName(name);
        return trainingTypeRepository.save(type);
    }
}

