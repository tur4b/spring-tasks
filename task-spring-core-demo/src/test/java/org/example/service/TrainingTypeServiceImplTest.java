package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.dao.TrainingTypeRepository;
import org.example.dto.response.TrainingTypeDTO;
import org.example.entity.TrainingType;
import org.example.entity.TrainingTypeName;
import org.example.mapper.TrainingTypeMapper;
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

    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    @Test
    @DisplayName("findById - throws EntityNotFoundException when id does not exist")
    void findById_NotFound_Throws() {
        when(trainingTypeRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingTypeService.findById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TrainingType found with ID: 999");
    }

    @Test
    @DisplayName("findAll - maps all entities to DTOs")
    void findAll_MapsEntities() {
        TrainingType type = new TrainingType();
        type.setId(1);
        type.setName(TrainingTypeName.CARDIO);

        TrainingTypeDTO dto = new TrainingTypeDTO(1, TrainingTypeName.CARDIO);

        when(trainingTypeRepository.findAll()).thenReturn(List.of(type));
        when(trainingTypeMapper.toDTO(type)).thenReturn(dto);

        List<TrainingTypeDTO> result = trainingTypeService.findAll();

        assertThat(result).containsExactly(dto);
    }
}

