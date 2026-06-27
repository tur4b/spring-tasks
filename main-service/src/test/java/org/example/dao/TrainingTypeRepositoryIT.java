package org.example.dao;

import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("TrainingTypeRepository Integration Tests")
class TrainingTypeRepositoryIT {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DisplayName("save and findAll returns persisted training types")
    void saveAndFindAll_ReturnsPersistedTypes() {
        TrainingType cardio = new TrainingType();
        cardio.setName(TrainingTypeName.CARDIO);
        TrainingType strength = new TrainingType();
        strength.setName(TrainingTypeName.STRENGTH);

        trainingTypeRepository.saveAndFlush(cardio);
        trainingTypeRepository.saveAndFlush(strength);

        assertThat(trainingTypeRepository.findAll()).hasSize(2);
    }
}

