package org.example.service;

import org.example.dao.TrainingTypeRepository;
import org.example.dto.response.TrainingTypeDTO;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.exception.model.NotFoundException;
import org.example.service.impl.TrainingTypeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingTypeServiceImpl Unit Tests")
class TrainingTypeServiceImplTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    @Test
    @DisplayName("findAll maps all training types")
    void findAll_MapsAllTrainingTypes() {
        TrainingType cardio = new TrainingType();
        cardio.setId(1);
        cardio.setName(TrainingTypeName.CARDIO);
        TrainingType strength = new TrainingType();
        strength.setId(2);
        strength.setName(TrainingTypeName.STRENGTH);

        when(trainingTypeRepository.findAll()).thenReturn(List.of(cardio, strength));

        assertThat(trainingTypeService.findAll()).containsExactly(
                new TrainingTypeDTO(1, TrainingTypeName.CARDIO),
                new TrainingTypeDTO(2, TrainingTypeName.STRENGTH)
        );
    }

    @Test
    @DisplayName("findById returns dto for existing training type")
    void findById_ReturnsDto() {
        TrainingType cardio = new TrainingType();
        cardio.setId(1);
        cardio.setName(TrainingTypeName.CARDIO);
        when(trainingTypeRepository.findById(1)).thenReturn(Optional.of(cardio));

        assertThat(trainingTypeService.findById(1)).isEqualTo(new TrainingTypeDTO(1, TrainingTypeName.CARDIO));
    }

    @Test
    @DisplayName("findById throws not found for missing training type")
    void findById_ThrowsWhenMissing() {
        when(trainingTypeRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingTypeService.findById(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("TrainingType found with ID: 99");
    }
}

