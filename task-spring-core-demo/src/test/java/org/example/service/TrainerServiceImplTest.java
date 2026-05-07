package org.example.service;

import org.example.dao.TrainerDAO;
import org.example.dto.request.TrainerCreateRequest;
import org.example.dto.request.TrainerUpdateRequest;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.TrainerDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.mapper.TrainerMapper;
import org.example.service.api.UserService;
import org.example.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerServiceImpl Unit Tests")
class TrainerServiceImplTest {

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainerDAO trainerDAO;

    @Mock
    private UserService userService;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private Trainer trainer;
    private TrainerDTO trainerDTO;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUserId(10L);
        trainer.setSpecialization(TrainingType.CARDIO);

        trainerDTO = new TrainerDTO(1L, 10L, TrainingType.CARDIO, LocalDateTime.now());
        userDTO = new UserDTO(10L, "Jane", "Smith", "jane.smith", LocalDateTime.now());
    }

    @Test
    @DisplayName("findAll - returns list of TrainerDTOs")
    void findAll_ReturnsMappedDTOs() {
        when(trainerDAO.findAll()).thenReturn(List.of(trainer));
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDTO);

        List<TrainerDTO> result = trainerService.findAll();

        assertThat(result).hasSize(1).containsExactly(trainerDTO);
        verify(trainerDAO).findAll();
    }

    @Test
    @DisplayName("findAll - returns empty list when no trainers exist")
    void findAll_EmptyList() {
        when(trainerDAO.findAll()).thenReturn(List.of());

        assertThat(trainerService.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findTrainerById - returns TrainerDTO when found")
    void findTrainerById_Found_ReturnsDTO() {
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDTO);

        TrainerDTO result = trainerService.findTrainerById(1L);

        assertThat(result).isEqualTo(trainerDTO);
    }

    @Test
    @DisplayName("findTrainerById - throws RuntimeException when not found")
    void findTrainerById_NotFound_ThrowsException() {
        when(trainerDAO.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.findTrainerById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainer not found with ID: 99");
    }

    @Test
    @DisplayName("createTrainer - creates trainer and returns TrainerDTO")
    void createTrainer_ValidRequest_ReturnsDTO() {
        TrainerCreateRequest request = new TrainerCreateRequest("Smith", "Jane", TrainingType.CARDIO);

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userDTO);
        when(trainerMapper.toEntity(request)).thenReturn(trainer);
        when(trainerDAO.create(trainer)).thenReturn(trainer);
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDTO);

        TrainerDTO result = trainerService.createTrainer(request);

        assertThat(result).isEqualTo(trainerDTO);
        verify(userService).createUser(any(UserCreateRequest.class));
        verify(trainerDAO).create(trainer);
    }

    @Test
    @DisplayName("createTrainer - throws IllegalArgumentException for null request")
    void createTrainer_NullRequest_ThrowsException() {
        assertThatThrownBy(() -> trainerService.createTrainer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TrainerCreateRequest cannot be null");
    }

    @Test
    @DisplayName("updateTrainer - updates trainer and returns updated TrainerDTO")
    void updateTrainer_ValidRequest_ReturnsUpdatedDTO() {
        TrainerUpdateRequest request = new TrainerUpdateRequest("Smith", "Jane", TrainingType.STRENGTH);

        when(trainerDAO.findById(1L)).thenReturn(Optional.of(trainer));
        when(userService.findUserById(trainer.getUserId())).thenReturn(userDTO);
        when(userService.updateUser(eq(userDTO.id()), any(UserUpdateRequest.class))).thenReturn(userDTO);
        when(trainerMapper.toDTO(trainer)).thenReturn(trainerDTO);

        TrainerDTO result = trainerService.updateTrainer(1L, request);

        assertThat(result).isEqualTo(trainerDTO);
        verify(trainerDAO).update(trainer);
        assertThat(trainer.getSpecialization()).isEqualTo(TrainingType.STRENGTH);
    }

    @Test
    @DisplayName("updateTrainer - throws IllegalArgumentException for null trainerId")
    void updateTrainer_NullId_ThrowsException() {
        assertThatThrownBy(() -> trainerService.updateTrainer(null, new TrainerUpdateRequest("A", "B", TrainingType.CARDIO)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updateTrainer - throws IllegalArgumentException for null request")
    void updateTrainer_NullRequest_ThrowsException() {
        assertThatThrownBy(() -> trainerService.updateTrainer(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("updateTrainer - throws RuntimeException when trainer not found")
    void updateTrainer_NotFound_ThrowsException() {
        when(trainerDAO.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateTrainer(99L, new TrainerUpdateRequest("A", "B", TrainingType.CARDIO)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainer not found with ID: 99");
    }

    @Test
    @DisplayName("deleteTrainer - returns true when deletion succeeds")
    void deleteTrainer_ReturnsTrue() {
        when(trainerDAO.deleteById(1L)).thenReturn(true);

        assertThat(trainerService.deleteTrainer(1L)).isTrue();
        verify(trainerDAO).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTrainer - returns false when trainer not found")
    void deleteTrainer_ReturnsFalse() {
        when(trainerDAO.deleteById(99L)).thenReturn(false);

        assertThat(trainerService.deleteTrainer(99L)).isFalse();
    }

    @Test
    @DisplayName("existsById - returns true when trainer exists")
    void existsById_ReturnsTrue() {
        when(trainerDAO.existsById(1L)).thenReturn(true);

        assertThat(trainerService.existsById(1L)).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false when trainer does not exist")
    void existsById_ReturnsFalse() {
        when(trainerDAO.existsById(99L)).thenReturn(false);

        assertThat(trainerService.existsById(99L)).isFalse();
    }
}

