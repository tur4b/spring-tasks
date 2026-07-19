package org.example.workload.service;

import org.example.common.model.MonthSummary;
import org.example.common.model.TrainerSummary;
import org.example.workload.document.MonthSummaryDocument;
import org.example.workload.document.TrainerSummaryDocument;
import org.example.workload.document.YearSummaryDocument;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.repository.TrainerSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadQueryService Unit Tests")
class WorkloadQueryServiceTest {

    @Mock
    private TrainerSummaryRepository repository;

    private WorkloadQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new WorkloadQueryService(repository);
    }

    @Test
    @DisplayName("getMonthlySummary returns summary for existing month")
    void getMonthlySummary_ReturnsSummary() {
        when(repository.findByUsername("trainer.one"))
                .thenReturn(Optional.of(documentWithMonth("trainer.one", 2026, 6, 60)));

        MonthSummary summary = queryService.getMonthlySummary("trainer.one", 2026, 6);

        assertThat(summary.getMonth()).isEqualTo(6);
        assertThat(summary.getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    @DisplayName("getMonthlySummary throws WorkloadNotFoundException for unknown month")
    void getMonthlySummary_UnknownMonth_Throws() {
        when(repository.findByUsername("trainer.one"))
                .thenReturn(Optional.of(documentWithMonth("trainer.one", 2026, 5, 45)));

        assertThatThrownBy(() -> queryService.getMonthlySummary("trainer.one", 2026, 6))
                .isInstanceOf(WorkloadNotFoundException.class)
                .hasMessageContaining("2026-6");
    }

    @Test
    @DisplayName("getMonthlySummary throws WorkloadNotFoundException for unknown trainer")
    void getMonthlySummary_UnknownTrainer_Throws() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.getMonthlySummary("unknown", 2026, 6))
                .isInstanceOf(WorkloadNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    @DisplayName("getTrainerSummary returns full mapped summary")
    void getTrainerSummary_ReturnsSummary() {
        when(repository.findByUsername("trainer.one"))
                .thenReturn(Optional.of(documentWithMonth("trainer.one", 2026, 6, 60)));

        TrainerSummary summary = queryService.getTrainerSummary("trainer.one");

        assertThat(summary.getUsername()).isEqualTo("trainer.one");
        assertThat(summary.getFirstName()).isEqualTo("John");
        assertThat(summary.getLastName()).isEqualTo("Smith");
        assertThat(summary.isActive()).isTrue();
        assertThat(summary.getYears()).hasSize(1);
        assertThat(summary.getYears().get(0).getYear()).isEqualTo(2026);
        assertThat(summary.getYears().get(0).getMonths()).hasSize(1);
        assertThat(summary.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    @DisplayName("getTrainerSummary throws WorkloadNotFoundException for unknown trainer")
    void getTrainerSummary_NotFound_Throws() {
        when(repository.findByUsername("unknown.trainer")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.getTrainerSummary("unknown.trainer"))
                .isInstanceOf(WorkloadNotFoundException.class)
                .hasMessageContaining("unknown.trainer");
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