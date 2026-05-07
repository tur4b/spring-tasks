package org.example.dao;

import org.example.dao.impl.InMemoryTrainerDAO;
import org.example.entity.Trainer;
import org.example.entity.TrainingType;
import org.example.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InMemoryTrainerDAO Unit Tests")
class InMemoryTrainerDAOTest {

    @Mock
    private IdGenerator idGenerator;

    private Map<Long, Trainer> trainerMap;
    private InMemoryTrainerDAO trainerDAO;

    private Trainer buildTrainer(Long id, boolean active) {
        Trainer t = new Trainer();
        t.setId(id);
        t.setUserId(200L + id);
        t.setSpecialization(TrainingType.CARDIO);
        t.setActive(active);
        return t;
    }

    @BeforeEach
    void setUp() {
        trainerMap = new HashMap<>();
        trainerDAO = new InMemoryTrainerDAO(trainerMap, idGenerator);
    }

    @Test
    @DisplayName("findById - returns trainer when found")
    void findById_Found() {
        Trainer trainer = buildTrainer(1L, true);
        trainerMap.put(1L, trainer);

        Optional<Trainer> result = trainerDAO.findById(1L);

        assertThat(result).isPresent().contains(trainer);
    }

    @Test
    @DisplayName("findById - returns empty Optional when not found")
    void findById_NotFound() {
        assertThat(trainerDAO.findById(99L)).isEmpty();
    }

    @Test
    @DisplayName("findById - returns empty Optional for null ID")
    void findById_NullId() {
        assertThat(trainerDAO.findById(null)).isEmpty();
    }

    @Test
    @DisplayName("findById - returns empty Optional for negative ID")
    void findById_NegativeId() {
        assertThat(trainerDAO.findById(-5L)).isEmpty();
    }

    @Test
    @DisplayName("findAll - returns only active trainers")
    void findAll_ReturnsOnlyActive() {
        trainerMap.put(1L, buildTrainer(1L, true));
        trainerMap.put(2L, buildTrainer(2L, false)); // inactive
        trainerMap.put(3L, buildTrainer(3L, true));

        List<Trainer> result = trainerDAO.findAll();

        assertThat(result).hasSize(2)
                .extracting(Trainer::getId)
                .containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    @DisplayName("findAll - returns empty list when map is empty")
    void findAll_EmptyStore() {
        assertThat(trainerDAO.findAll()).isEmpty();
    }

    @Test
    @DisplayName("create - persists trainer and assigns generated ID")
    void create_Success() {
        when(idGenerator.getNextId("Trainer")).thenReturn(20L);
        Trainer trainer = buildTrainer(0L, true);

        Trainer result = trainerDAO.create(trainer);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(trainerMap).containsKey(20L);
    }

    @Test
    @DisplayName("create - throws IllegalArgumentException for null trainer")
    void create_NullTrainer() {
        assertThatThrownBy(() -> trainerDAO.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trainer cannot be null");
    }

    @Test
    @DisplayName("update - updates existing trainer in the store")
    void update_Success() {
        Trainer trainer = buildTrainer(1L, true);
        trainerMap.put(1L, trainer);

        trainer.setSpecialization(TrainingType.STRENGTH);
        Trainer result = trainerDAO.update(trainer);

        assertThat(result.getSpecialization()).isEqualTo(TrainingType.STRENGTH);
        assertThat(trainerMap.get(1L).getSpecialization()).isEqualTo(TrainingType.STRENGTH);
    }

    @Test
    @DisplayName("update - throws RuntimeException when trainer not in store")
    void update_NotFound() {
        Trainer trainer = buildTrainer(99L, true);

        assertThatThrownBy(() -> trainerDAO.update(trainer))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainer not found with ID: 99");
    }

    @Test
    @DisplayName("update - throws IllegalArgumentException for null trainer")
    void update_NullTrainer() {
        assertThatThrownBy(() -> trainerDAO.update(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("update - throws IllegalArgumentException for trainer with null ID")
    void update_NullId() {
        Trainer trainer = new Trainer();
        trainer.setId(null);

        assertThatThrownBy(() -> trainerDAO.update(trainer))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deleteById - soft-deletes existing trainer and returns true")
    void deleteById_Success() {
        trainerMap.put(1L, buildTrainer(1L, true));

        boolean result = trainerDAO.deleteById(1L);

        assertThat(result).isTrue();
        assertThat(trainerMap.get(1L).isActive()).isFalse();
        assertThat(trainerMap.get(1L).getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteById - returns false when trainer not found")
    void deleteById_NotFound() {
        assertThat(trainerDAO.deleteById(99L)).isFalse();
    }

    @Test
    @DisplayName("existsById - returns true when trainer exists")
    void existsById_True() {
        trainerMap.put(1L, buildTrainer(1L, true));

        assertThat(trainerDAO.existsById(1L)).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false when trainer absent")
    void existsById_False() {
        assertThat(trainerDAO.existsById(99L)).isFalse();
    }
}

