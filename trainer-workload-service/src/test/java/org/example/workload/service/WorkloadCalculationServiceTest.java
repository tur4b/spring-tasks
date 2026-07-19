package org.example.workload.service;

import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.ActionType;
import org.example.workload.document.MonthSummaryDocument;
import org.example.workload.document.TrainerSummaryDocument;
import org.example.workload.document.YearSummaryDocument;
import org.example.workload.repository.TrainerSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadCalculationService Unit Tests")
class WorkloadCalculationServiceTest {

    @Mock
    private TrainerSummaryRepository repository;

    private WorkloadCalculationService service;

    @BeforeEach
    void setUp() {
        service = new WorkloadCalculationService(repository);
    }

    @Test
    @DisplayName("ADD creates a new document when trainer does not exist")
    void add_CreatesNewDocument() {
        when(repository.findByUsername("trainer.one")).thenReturn(Optional.empty());

        service.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 15), 60));

        ArgumentCaptor<TrainerSummaryDocument> captor = ArgumentCaptor.forClass(TrainerSummaryDocument.class);
        verify(repository).save(captor.capture());
        TrainerSummaryDocument saved = captor.getValue();

        assertThat(saved.getUsername()).isEqualTo("trainer.one");
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Smith");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getYears()).hasSize(1);
        assertThat(saved.getYears().get(0).getYear()).isEqualTo(2026);
        assertThat(saved.getYears().get(0).getMonths()).hasSize(1);
        assertThat(saved.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(6);
        assertThat(saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    @DisplayName("ADD accumulates duration for existing month")
    void add_AccumulatesExistingDuration() {
        TrainerSummaryDocument existing = documentWithMonth("trainer.one", 2026, 6, 40);
        when(repository.findByUsername("trainer.one")).thenReturn(Optional.of(existing));

        service.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 20), 30));

        ArgumentCaptor<TrainerSummaryDocument> captor = ArgumentCaptor.forClass(TrainerSummaryDocument.class);
        verify(repository).save(captor.capture());
        int duration = captor.getValue().getYears().get(0).getMonths().get(0).getTrainingSummaryDuration();
        assertThat(duration).isEqualTo(70);
    }

    @Test
    @DisplayName("DELETE decreases monthly duration")
    void delete_DecreasesMonthlyDuration() {
        TrainerSummaryDocument existing = documentWithMonth("trainer.one", 2026, 6, 60);
        when(repository.findByUsername("trainer.one")).thenReturn(Optional.of(existing));

        service.processEvent(event(ActionType.DELETE, "trainer.one", LocalDate.of(2026, 6, 15), 20));

        ArgumentCaptor<TrainerSummaryDocument> captor = ArgumentCaptor.forClass(TrainerSummaryDocument.class);
        verify(repository).save(captor.capture());
        int duration = captor.getValue().getYears().get(0).getMonths().get(0).getTrainingSummaryDuration();
        assertThat(duration).isEqualTo(40);
    }

    @Test
    @DisplayName("DELETE clamps duration at zero")
    void delete_ClampAtZero() {
        TrainerSummaryDocument existing = documentWithMonth("trainer.one", 2026, 6, 30);
        when(repository.findByUsername("trainer.one")).thenReturn(Optional.of(existing));

        service.processEvent(event(ActionType.DELETE, "trainer.one", LocalDate.of(2026, 6, 15), 50));

        ArgumentCaptor<TrainerSummaryDocument> captor = ArgumentCaptor.forClass(TrainerSummaryDocument.class);
        verify(repository).save(captor.capture());
        int duration = captor.getValue().getYears().get(0).getMonths().get(0).getTrainingSummaryDuration();
        assertThat(duration).isEqualTo(0);
    }

    @Test
    @DisplayName("ADD creates separate buckets for different months")
    void add_BucketsByYearAndMonth() {
        TrainerSummaryDocument existing = documentWithMonth("trainer.one", 2026, 5, 45);
        when(repository.findByUsername("trainer.one")).thenReturn(Optional.of(existing));

        service.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 10), 55));

        ArgumentCaptor<TrainerSummaryDocument> captor = ArgumentCaptor.forClass(TrainerSummaryDocument.class);
        verify(repository).save(captor.capture());
        TrainerSummaryDocument saved = captor.getValue();

        int mayDuration = saved.getYears().get(0).getMonths().stream()
                .filter(m -> m.getMonth() == 5).findFirst().map(MonthSummaryDocument::getTrainingSummaryDuration).orElse(-1);
        int juneDuration = saved.getYears().get(0).getMonths().stream()
                .filter(m -> m.getMonth() == 6).findFirst().map(MonthSummaryDocument::getTrainingSummaryDuration).orElse(-1);

        assertThat(mayDuration).isEqualTo(45);
        assertThat(juneDuration).isEqualTo(55);
    }

    private WorkloadEventRequest event(ActionType actionType, String username, LocalDate date, int duration) {
        return new WorkloadEventRequest(username, "John", "Smith", true, date, duration, actionType);
    }

    private TrainerSummaryDocument documentWithMonth(String username, int year, int month, int duration) {
        MonthSummaryDocument monthDoc = MonthSummaryDocument.builder()
                .month(month)
                .trainingSummaryDuration(duration)
                .build();
        YearSummaryDocument yearDoc = YearSummaryDocument.builder()
                .year(year)
                .months(new ArrayList<>(List.of(monthDoc)))
                .build();
        return TrainerSummaryDocument.builder()
                .username(username)
                .firstName("John")
                .lastName("Smith")
                .isActive(true)
                .years(new ArrayList<>(List.of(yearDoc)))
                .build();
    }
}