package org.example.dao;

import org.example.dao.impl.InMemoryTrainingDAO;
import org.example.entity.Training;
import org.example.entity.TrainingType;
import org.example.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InMemoryTrainingDAO Unit Tests")
class InMemoryTrainingDAOTest {

    @Mock
    private IdGenerator idGenerator;

    private Map<Long, Training> trainingMap;
    private InMemoryTrainingDAO trainingDAO;

    private Training buildTraining(Long id, boolean active) {
        Training t = new Training();
        t.setId(id);
        t.setTraineeId(10L);
        t.setTrainerId(20L);
        t.setName("Training " + id);
        t.setType(TrainingType.CARDIO);
        t.setDate(LocalDate.of(2026, 5, 7));
        t.setDuration(60);
        t.setActive(active);
        return t;
    }

    @BeforeEach
    void setUp() {
        trainingMap = new HashMap<>();
        trainingDAO = new InMemoryTrainingDAO(trainingMap, idGenerator);
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById - returns training when found")
    void findById_Found() {
        Training training = buildTraining(1L, true);
        trainingMap.put(1L, training);

        Optional<Training> result = trainingDAO.findById(1L);

        assertThat(result).isPresent().contains(training);
    }

    @Test
    @DisplayName("findById - returns empty Optional when not found")
    void findById_NotFound() {
        assertThat(trainingDAO.findById(99L)).isEmpty();
    }

    @Test
    @DisplayName("findById - returns empty Optional for null ID")
    void findById_NullId() {
        assertThat(trainingDAO.findById(null)).isEmpty();
    }

    @Test
    @DisplayName("findById - returns empty Optional for negative ID")
    void findById_NegativeId() {
        assertThat(trainingDAO.findById(-3L)).isEmpty();
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll - returns only active trainings")
    void findAll_ReturnsOnlyActive() {
        trainingMap.put(1L, buildTraining(1L, true));
        trainingMap.put(2L, buildTraining(2L, false)); // inactive
        trainingMap.put(3L, buildTraining(3L, true));

        List<Training> result = trainingDAO.findAll();

        assertThat(result).hasSize(2)
                .extracting(Training::getId)
                .containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    @DisplayName("findAll - returns empty list when map is empty")
    void findAll_EmptyStore() {
        assertThat(trainingDAO.findAll()).isEmpty();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create - persists training and assigns generated ID")
    void create_Success() {
        when(idGenerator.getNextId("Training")).thenReturn(30L);
        Training training = buildTraining(0L, true);

        Training result = trainingDAO.create(training);

        assertThat(result.getId()).isEqualTo(30L);
        assertThat(trainingMap).containsKey(30L);
    }

    @Test
    @DisplayName("create - throws IllegalArgumentException for null training")
    void create_NullTraining() {
        assertThatThrownBy(() -> trainingDAO.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Training cannot be null");
    }

    @Test
    @DisplayName("update - updates existing training in the store")
    void update_Success() {
        Training training = buildTraining(1L, true);
        trainingMap.put(1L, training);

        training.setName("Updated Training");
        training.setDuration(90);
        Training result = trainingDAO.update(training);

        assertThat(result.getName()).isEqualTo("Updated Training");
        assertThat(result.getDuration()).isEqualTo(90);
        assertThat(trainingMap.get(1L).getName()).isEqualTo("Updated Training");
    }

    @Test
    @DisplayName("update - throws RuntimeException when training not in store")
    void update_NotFound() {
        Training training = buildTraining(99L, true);

        assertThatThrownBy(() -> trainingDAO.update(training))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Training not found with ID: 99");
    }

    @Test
    @DisplayName("update - throws IllegalArgumentException for null training")
    void update_NullTraining() {
        assertThatThrownBy(() -> trainingDAO.update(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deleteById - soft-deletes existing training and returns true")
    void deleteById_Success() {
        trainingMap.put(1L, buildTraining(1L, true));

        boolean result = trainingDAO.deleteById(1L);

        assertThat(result).isTrue();
        assertThat(trainingMap.get(1L).isActive()).isFalse();
        assertThat(trainingMap.get(1L).getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteById - returns false when training not found")
    void deleteById_NotFound() {
        assertThat(trainingDAO.deleteById(99L)).isFalse();
    }

    @Test
    @DisplayName("existsById - returns true when training exists")
    void existsById_True() {
        trainingMap.put(1L, buildTraining(1L, true));

        assertThat(trainingDAO.existsById(1L)).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false when training absent")
    void existsById_False() {
        assertThat(trainingDAO.existsById(99L)).isFalse();
    }
}

