package org.example.workload.service;

import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.ActionType;
import org.example.workload.store.WorkloadStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkloadCalculationService Unit Tests")
class WorkloadCalculationServiceTest {

    private WorkloadStore workloadStore;
    private WorkloadCalculationService workloadCalculationService;

    @BeforeEach
    void setUp() {
        workloadStore = new WorkloadStore();
        workloadCalculationService = new WorkloadCalculationService(workloadStore);
    }

    @Test
    @DisplayName("ADD increases monthly duration")
    void add_IncreasesMonthlyDuration() {
        workloadCalculationService.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 15), 60));
        workloadCalculationService.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 20), 30));

        assertThat(workloadStore.findMonthSummary("trainer.one", 2026, 6))
                .isPresent()
                .get()
                .extracting(summary -> summary.getTrainingSummaryDuration())
                .isEqualTo(90);
    }

    @Test
    @DisplayName("DELETE decreases monthly duration")
    void delete_DecreasesMonthlyDuration() {
        workloadCalculationService.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 15), 60));
        workloadCalculationService.processEvent(event(ActionType.DELETE, "trainer.one", LocalDate.of(2026, 6, 15), 20));

        assertThat(workloadStore.findMonthSummary("trainer.one", 2026, 6))
                .isPresent()
                .get()
                .extracting(summary -> summary.getTrainingSummaryDuration())
                .isEqualTo(40);
    }

    @Test
    @DisplayName("DELETE clamps duration at zero")
    void delete_ClampAtZero() {
        workloadCalculationService.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 15), 30));
        workloadCalculationService.processEvent(event(ActionType.DELETE, "trainer.one", LocalDate.of(2026, 6, 15), 50));

        assertThat(workloadStore.findMonthSummary("trainer.one", 2026, 6))
                .isPresent()
                .get()
                .extracting(summary -> summary.getTrainingSummaryDuration())
                .isEqualTo(0);
    }

    @Test
    @DisplayName("ADD buckets workload by year and month")
    void add_BucketsByYearAndMonth() {
        workloadCalculationService.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 5, 10), 45));
        workloadCalculationService.processEvent(event(ActionType.ADD, "trainer.one", LocalDate.of(2026, 6, 10), 55));

        assertThat(workloadStore.findMonthSummary("trainer.one", 2026, 5))
                .isPresent()
                .get()
                .extracting(summary -> summary.getTrainingSummaryDuration())
                .isEqualTo(45);

        assertThat(workloadStore.findMonthSummary("trainer.one", 2026, 6))
                .isPresent()
                .get()
                .extracting(summary -> summary.getTrainingSummaryDuration())
                .isEqualTo(55);
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
