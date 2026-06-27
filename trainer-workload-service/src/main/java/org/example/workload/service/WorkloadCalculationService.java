package org.example.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.logging.TransactionContext;
import org.example.common.model.ActionType;
import org.example.common.model.MonthSummary;
import org.example.common.model.TrainerSummary;
import org.example.workload.store.WorkloadStore;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadCalculationService {

    private final WorkloadStore workloadStore;
    private final ConcurrentHashMap<String, Object> trainerLocks = new ConcurrentHashMap<>();

    public void processEvent(WorkloadEventRequest request) {
        log.info("[transactionId={}] OPERATION processWorkload action={} trainer={} date={} duration={}",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                request.actionType(),
                request.trainerUsername(),
                request.trainingDate(),
                request.trainingDuration());

        Object lock = trainerLocks.computeIfAbsent(request.trainerUsername(), key -> new Object());
        synchronized (lock) {
            TrainerSummary summary = workloadStore.getOrCreate(request);
            workloadStore.updateTrainerMetadata(summary, request);

            int year = request.trainingDate().getYear();
            int month = request.trainingDate().getMonthValue();
            MonthSummary monthSummary = workloadStore.getOrCreateMonthSummary(summary, year, month);

            int updatedDuration = monthSummary.getTrainingSummaryDuration();
            if (request.actionType() == ActionType.ADD) {
                updatedDuration += request.trainingDuration();
            } else {
                updatedDuration = Math.max(0, updatedDuration - request.trainingDuration());
            }
            monthSummary.setTrainingSummaryDuration(updatedDuration);

            log.info("[transactionId={}] OPERATION updatedMonthlySummary trainer={} year={} month={} totalDuration={}",
                    MDC.get(TransactionContext.TRANSACTION_MDC_KEY),
                    request.trainerUsername(),
                    year,
                    month,
                    updatedDuration);
        }
    }
}
