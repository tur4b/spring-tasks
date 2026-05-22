package org.example.dao;

import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.testsupport.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainingTypeRepository - DAO Slice Integration Tests")
class TrainingTypeRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DisplayName("save + existsById - persists training type")
    void save_PersistsType() {
        TrainingType type = new TrainingType();
        type.setName(TrainingTypeName.CARDIO);

        TrainingType saved = trainingTypeRepository.save(type);

        assertThat(saved.getId()).isNotNull();
        assertThat(trainingTypeRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("findAll - returns all saved types")
    void findAll_ReturnsSavedTypes() {
        TrainingType t1 = new TrainingType();
        t1.setName(TrainingTypeName.CARDIO);
        TrainingType t2 = new TrainingType();
        t2.setName(TrainingTypeName.STRENGTH);
        trainingTypeRepository.saveAll(List.of(t1, t2));

        assertThat(trainingTypeRepository.findAll()).hasSizeGreaterThanOrEqualTo(2);
    }
}

