package org.example.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.logging.TransactionContext;
import org.example.common.model.MonthSummary;
import org.example.common.model.TrainerSummary;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.store.WorkloadStore;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadQueryService {

    private final WorkloadStore workloadStore;

    public MonthSummary getMonthlySummary(String username, int year, int month) {
        log.info("[transactionId={}] OPERATION getMonthlySummary trainer={} year={} month={}",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                username,
                year,
                month);

        return workloadStore.findMonthSummary(username, year, month)
                .orElseThrow(() -> new WorkloadNotFoundException(
                        "No workload found for trainer " + username + " in " + year + "-" + month));
    }

    public TrainerSummary getTrainerSummary(String username) {
        log.info("[transactionId={}] OPERATION getTrainerSummary trainer={}",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                username);

        return workloadStore.findByUsername(username)
                .orElseThrow(() -> new WorkloadNotFoundException("No workload found for trainer " + username));
    }
}
