package org.example.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.logging.TransactionContext;
import org.example.common.model.MonthSummary;
import org.example.common.model.TrainerSummary;
import org.example.common.model.YearSummary;
import org.example.workload.document.TrainerSummaryDocument;
import org.example.workload.exception.WorkloadNotFoundException;
import org.example.workload.repository.TrainerSummaryRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadQueryService {

    private final TrainerSummaryRepository repository;

    public MonthSummary getMonthlySummary(String username, int year, int month) {
        log.info("[transactionId={}] OPERATION getMonthlySummary trainer={} year={} month={}",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY), username, year, month);

        TrainerSummaryDocument doc = repository.findByUsername(username)
                .orElseThrow(() -> new WorkloadNotFoundException("No workload found for trainer " + username));

        return doc.getYears().stream()
                .filter(y -> y.getYear() == year)
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .map(m -> MonthSummary.builder()
                        .month(m.getMonth())
                        .trainingSummaryDuration(m.getTrainingSummaryDuration())
                        .build())
                .orElseThrow(() -> new WorkloadNotFoundException(
                        "No workload found for trainer " + username + " in " + year + "-" + month));
    }

    public TrainerSummary getTrainerSummary(String username) {
        log.info("[transactionId={}] OPERATION getTrainerSummary trainer={}",
                MDC.get(TransactionContext.TRANSACTION_MDC_KEY), username);

        TrainerSummaryDocument doc = repository.findByUsername(username)
                .orElseThrow(() -> new WorkloadNotFoundException("No workload found for trainer " + username));

        return toModel(doc);
    }

    private TrainerSummary toModel(TrainerSummaryDocument doc) {
        List<YearSummary> years = doc.getYears().stream()
                .map(y -> {
                    List<MonthSummary> months = y.getMonths().stream()
                            .map(m -> MonthSummary.builder()
                                    .month(m.getMonth())
                                    .trainingSummaryDuration(m.getTrainingSummaryDuration())
                                    .build())
                            .toList();
                    return YearSummary.builder()
                            .year(y.getYear())
                            .months(months)
                            .build();
                })
                .toList();

        return TrainerSummary.builder()
                .username(doc.getUsername())
                .firstName(doc.getFirstName())
                .lastName(doc.getLastName())
                .isActive(doc.isActive())
                .years(years)
                .build();
    }
}