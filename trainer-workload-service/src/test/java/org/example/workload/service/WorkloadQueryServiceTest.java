package org.example.workload.service;

import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.ActionType;
import org.example.common.model.MonthSummary;
import org.example.common.model.TrainerSummary;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.store.WorkloadStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkloadQueryService Unit Tests")
class WorkloadQueryServiceTest {

    private WorkloadCalculationService workloadCalculationService;
    private WorkloadQueryService workloadQueryService;

    @BeforeEach
    void setUp() {
        WorkloadStore workloadStore = new WorkloadStore();
        workloadCalculationService = new WorkloadCalculationService(workloadStore);
        workloadQueryService = new WorkloadQueryService(workloadStore);
    }

    @Test
    @DisplayName("getMonthlySummary returns month summary for existing workload")
    void getMonthlySummary_ReturnsSummary() {
        workloadCalculationService.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 15), 60));

        MonthSummary summary = workloadQueryService.getMonthlySummary("trainer.one", 2026, 6);

        assertThat(summary.getMonth()).isEqualTo(6);
        assertThat(summary.getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    @DisplayName("getMonthlySummary throws when month workload is missing")
    void getMonthlySummary_NotFound() {
        workloadCalculationService.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 5, 10), 45));

        assertThatThrownBy(() -> workloadQueryService.getMonthlySummary("trainer.one", 2026, 6))
                .isInstanceOf(WorkloadNotFoundException.class)
                .hasMessageContaining("2026-6");
    }

    @Test
    @DisplayName("getTrainerSummary returns trainer summary for existing workload")
    void getTrainerSummary_ReturnsSummary() {
        workloadCalculationService.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 15), 60));

        TrainerSummary summary = workloadQueryService.getTrainerSummary("trainer.one");

        assertThat(summary.getUsername()).isEqualTo("trainer.one");
        assertThat(summary.getFirstName()).isEqualTo("John");
        assertThat(summary.getLastName()).isEqualTo("Smith");
        assertThat(summary.isActive()).isTrue();
    }

    @Test
    @DisplayName("getTrainerSummary throws when trainer workload is missing")
    void getTrainerSummary_NotFound() {
        assertThatThrownBy(() -> workloadQueryService.getTrainerSummary("unknown.trainer"))
                .isInstanceOf(WorkloadNotFoundException.class)
                .hasMessageContaining("unknown.trainer");
    }

    private WorkloadEventRequest event(ActionType actionType, String username, LocalDate date, int duration) {
        return new WorkloadEventRequest(
                username,
                "John",
                "Smith",
                true,
                date,
                duration,
                actionType
        );
    }
}
