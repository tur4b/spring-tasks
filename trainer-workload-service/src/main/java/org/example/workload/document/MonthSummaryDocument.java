package org.example.workload.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummaryDocument {
    private int month;
    private int trainingSummaryDuration;
}