package org.example.dao;

import org.example.dao.impl.InMemoryTraineeDAO;
import org.example.entity.Trainee;
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
@DisplayName("InMemoryTraineeDAO Unit Tests")
class InMemoryTraineeDAOTest {

    @Mock
    private IdGenerator idGenerator;

    private Map<Long, Trainee> traineeMap;
    private InMemoryTraineeDAO traineeDAO;

    private Trainee buildTrainee(Long id, boolean active) {
        Trainee t = new Trainee();
        t.setId(id);
        t.setUserId(100L + id);
        t.setAddress("Street " + id);
        t.setDateOfBirth(LocalDate.of(1990, 1, 1));
        t.setActive(active);
        return t;
    }

    @BeforeEach
    void setUp() {
        traineeMap = new HashMap<>();
        traineeDAO = new InMemoryTraineeDAO(traineeMap, idGenerator);
    }

    @Test
    @DisplayName("findById - returns trainee when found")
    void findById_Found() {
        Trainee trainee = buildTrainee(1L, true);
        traineeMap.put(1L, trainee);

        Optional<Trainee> result = traineeDAO.findById(1L);

        assertThat(result).isPresent().contains(trainee);
    }

    @Test
    @DisplayName("findById - returns empty Optional when not found")
    void findById_NotFound() {
        assertThat(traineeDAO.findById(99L)).isEmpty();
    }

    @Test
    @DisplayName("findById - returns empty Optional for null ID")
    void findById_NullId() {
        assertThat(traineeDAO.findById(null)).isEmpty();
    }

    @Test
    @DisplayName("findById - returns empty Optional for negative ID")
    void findById_NegativeId() {
        assertThat(traineeDAO.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findAll - returns only active trainees")
    void findAll_ReturnsOnlyActive() {
        traineeMap.put(1L, buildTrainee(1L, true));
        traineeMap.put(2L, buildTrainee(2L, false)); // inactive – should be excluded
        traineeMap.put(3L, buildTrainee(3L, true));

        List<Trainee> result = traineeDAO.findAll();

        assertThat(result).hasSize(2)
                .extracting(Trainee::getId)
                .containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    @DisplayName("findAll - returns empty list when store is empty")
    void findAll_EmptyStore() {
        assertThat(traineeDAO.findAll()).isEmpty();
    }

    @Test
    @DisplayName("create - persists trainee and assigns generated ID")
    void create_Success() {
        when(idGenerator.getNextId("Trainee")).thenReturn(10L);
        Trainee trainee = buildTrainee(0L, true);

        Trainee result = traineeDAO.create(trainee);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(traineeMap).containsKey(10L);
    }

    @Test
    @DisplayName("create - throws IllegalArgumentException for null trainee")
    void create_NullTrainee() {
        assertThatThrownBy(() -> traineeDAO.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trainee cannot be null");
    }

    @Test
    @DisplayName("update - updates existing trainee in the store")
    void update_Success() {
        Trainee trainee = buildTrainee(1L, true);
        traineeMap.put(1L, trainee);

        trainee.setAddress("New Address");
        Trainee result = traineeDAO.update(trainee);

        assertThat(result.getAddress()).isEqualTo("New Address");
        assertThat(traineeMap.get(1L).getAddress()).isEqualTo("New Address");
    }

    @Test
    @DisplayName("update - throws RuntimeException when trainee not present in store")
    void update_NotFound() {
        Trainee trainee = buildTrainee(99L, true);

        assertThatThrownBy(() -> traineeDAO.update(trainee))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Trainee not found with ID: 99");
    }

    @Test
    @DisplayName("update - throws IllegalArgumentException for null trainee")
    void update_NullTrainee() {
        assertThatThrownBy(() -> traineeDAO.update(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("update - throws IllegalArgumentException for trainee with null ID")
    void update_NullId() {
        Trainee trainee = new Trainee();
        trainee.setId(null);

        assertThatThrownBy(() -> traineeDAO.update(trainee))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deleteById - soft-deletes existing trainee and returns true")
    void deleteById_Success() {
        traineeMap.put(1L, buildTrainee(1L, true));

        boolean result = traineeDAO.deleteById(1L);

        assertThat(result).isTrue();
        assertThat(traineeMap.get(1L).isActive()).isFalse();
        assertThat(traineeMap.get(1L).getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteById - returns false when trainee not found")
    void deleteById_NotFound() {
        boolean result = traineeDAO.deleteById(99L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("existsById - returns true when trainee exists")
    void existsById_True() {
        traineeMap.put(1L, buildTrainee(1L, true));

        assertThat(traineeDAO.existsById(1L)).isTrue();
    }

    @Test
    @DisplayName("existsById - returns false when trainee absent")
    void existsById_False() {
        assertThat(traineeDAO.existsById(99L)).isFalse();
    }
}

