package org.example.workload.store;

import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.MonthSummary;
import org.example.common.model.TrainerSummary;
import org.example.common.model.YearSummary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkloadStore {

    private final Map<String, TrainerSummary> summaries = new ConcurrentHashMap<>();

    public TrainerSummary getOrCreate(WorkloadEventRequest request) {
        return summaries.computeIfAbsent(request.trainerUsername(), username -> TrainerSummary.builder()
                .username(username)
                .firstName(request.trainerFirstName())
                .lastName(request.trainerLastName())
                .isActive(request.isActive())
                .build());
    }

    public void updateTrainerMetadata(TrainerSummary summary, WorkloadEventRequest request) {
        summary.setFirstName(request.trainerFirstName());
        summary.setLastName(request.trainerLastName());
        summary.setActive(request.isActive());
    }

    public Optional<TrainerSummary> findByUsername(String username) {
        return Optional.ofNullable(summaries.get(username));
    }

    public Optional<MonthSummary> findMonthSummary(String username, int year, int month) {
        return findByUsername(username)
                .flatMap(summary -> summary.getYears().stream()
                        .filter(yearSummary -> yearSummary.getYear() == year)
                        .findFirst()
                        .flatMap(yearSummary -> yearSummary.getMonths().stream()
                                .filter(monthSummary -> monthSummary.getMonth() == month)
                                .findFirst()));
    }

    public MonthSummary getOrCreateMonthSummary(TrainerSummary summary, int year, int month) {
        YearSummary yearSummary = summary.getYears().stream()
                .filter(existing -> existing.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearSummary created = YearSummary.builder().year(year).build();
                    summary.getYears().add(created);
                    return created;
                });

        return yearSummary.getMonths().stream()
                .filter(existing -> existing.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary created = MonthSummary.builder()
                            .month(month)
                            .trainingSummaryDuration(0)
                            .build();
                    yearSummary.getMonths().add(created);
                    return created;
                });
    }

    public void clear() {
        summaries.clear();
    }
}
