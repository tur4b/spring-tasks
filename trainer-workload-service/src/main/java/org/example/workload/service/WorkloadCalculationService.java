package org.example.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.WorkloadEventRequest;
import org.example.common.logging.TransactionContext;
import org.example.common.model.ActionType;
import org.example.workload.document.MonthSummaryDocument;
import org.example.workload.document.TrainerSummaryDocument;
import org.example.workload.document.YearSummaryDocument;
import org.example.workload.repository.TrainerSummaryRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadCalculationService {

    private final TrainerSummaryRepository repository;

    public void processEvent(WorkloadEventRequest request) {
        String txId = MDC.get(TransactionContext.TRANSACTION_MDC_KEY);
        log.info("[transactionId={}] TRANSACTION processWorkload started action={} trainer={} date={} duration={}",
                txId,
                request.actionType(),
                request.trainerUsername(),
                request.trainingDate(),
                request.trainingDuration());

        TrainerSummaryDocument doc = repository.findByUsername(request.trainerUsername())
                .orElseGet(() -> {
                    log.info("[transactionId={}] OPERATION creating new trainer document for username={}",
                            txId, request.trainerUsername());
                    return TrainerSummaryDocument.builder()
                            .username(request.trainerUsername())
                            .build();
                });

        doc.setFirstName(request.trainerFirstName());
        doc.setLastName(request.trainerLastName());
        doc.setActive(request.isActive());

        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();

        YearSummaryDocument yearDoc = doc.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    log.info("[transactionId={}] OPERATION creating year entry year={} for trainer={}",
                            txId, year, request.trainerUsername());
                    YearSummaryDocument y = YearSummaryDocument.builder().year(year).build();
                    doc.getYears().add(y);
                    return y;
                });

        MonthSummaryDocument monthDoc = yearDoc.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    log.info("[transactionId={}] OPERATION creating month entry year={} month={} for trainer={}",
                            txId, year, month, request.trainerUsername());
                    MonthSummaryDocument m = MonthSummaryDocument.builder()
                            .month(month)
                            .trainingSummaryDuration(0)
                            .build();
                    yearDoc.getMonths().add(m);
                    return m;
                });

        int currentDuration = monthDoc.getTrainingSummaryDuration();
        int updatedDuration;
        if (request.actionType() == ActionType.ADD) {
            updatedDuration = currentDuration + request.trainingDuration();
        } else {
            updatedDuration = Math.max(0, currentDuration - request.trainingDuration());
        }
        monthDoc.setTrainingSummaryDuration(updatedDuration);

        repository.save(doc);

        log.info("[transactionId={}] OPERATION updatedMonthlySummary trainer={} year={} month={} totalDuration={}",
                txId, request.trainerUsername(), year, month, updatedDuration);
    }
}