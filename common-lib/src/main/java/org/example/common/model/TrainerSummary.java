package org.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerSummary {

    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;

    @Builder.Default
    private List<YearSummary> years = new ArrayList<>();
}
