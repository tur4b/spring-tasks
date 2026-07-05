package org.example.workload.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.dto.WorkloadEntryRequest;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.ActionType;
import org.example.common.model.MonthSummary;
import org.example.common.model.TrainerSummary;
import org.example.workload.service.WorkloadCalculationService;
import org.example.workload.service.WorkloadQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trainers/{username}/workloads")
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadCalculationService workloadCalculationService;
    private final WorkloadQueryService workloadQueryService;

    @PostMapping
    public ResponseEntity<Void> addWorkload(@PathVariable("username") String username,
                                            @Valid @RequestBody WorkloadEntryRequest request) {
        workloadCalculationService.processEvent(toEvent(username, request, ActionType.ADD));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteWorkload(@PathVariable("username") String username,
                                               @Valid @RequestBody WorkloadEntryRequest request) {
        workloadCalculationService.processEvent(toEvent(username, request, ActionType.DELETE));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<TrainerSummary> getTrainerWorkload(@PathVariable("username") String username) {
        TrainerSummary summary = workloadQueryService.getTrainerSummary(username);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/years/{year}/months/{month}")
    public ResponseEntity<MonthSummary> getMonthlyWorkload(@PathVariable("username") String username,
                                                           @PathVariable("year") int year,
                                                           @PathVariable("month") int month) {
        MonthSummary summary = workloadQueryService.getMonthlySummary(username, year, month);
        return ResponseEntity.ok(summary);
    }

    private WorkloadEventRequest toEvent(String username, WorkloadEntryRequest request, ActionType actionType) {
        return new WorkloadEventRequest(
                username,
                request.trainerFirstName(),
                request.trainerLastName(),
                request.isActive(),
                request.trainingDate(),
                request.trainingDuration(),
                actionType
        );
    }
}
