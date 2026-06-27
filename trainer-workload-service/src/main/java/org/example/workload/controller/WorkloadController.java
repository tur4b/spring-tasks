package org.example.workload.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.model.MonthSummary;
import org.example.common.model.TrainerSummary;
import org.example.workload.service.WorkloadCalculationService;
import org.example.workload.service.WorkloadQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadCalculationService workloadCalculationService;
    private final WorkloadQueryService workloadQueryService;

    @PostMapping("/workloads")
    public ResponseEntity<Void> submitWorkload(@Valid @RequestBody WorkloadEventRequest request) {
        workloadCalculationService.processEvent(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainers/{username}/workloads")
    public ResponseEntity<?> getTrainerWorkload(@PathVariable("username") String username,
                                               @RequestParam(name = "year", required = false) Integer year,
                                               @RequestParam(name = "month", required = false) Integer month) {
        if (year != null && month != null) {
            MonthSummary summary = workloadQueryService.getMonthlySummary(username, year, month);
            return ResponseEntity.ok(summary);
        }

        TrainerSummary summary = workloadQueryService.getTrainerSummary(username);
        return ResponseEntity.ok(summary);
    }
}
