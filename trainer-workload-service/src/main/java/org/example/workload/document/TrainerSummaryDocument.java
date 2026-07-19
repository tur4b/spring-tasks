package org.example.workload.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trainer_summaries")
@CompoundIndex(name = "idx_first_last_name", def = "{'firstName': 1, 'lastName': 1}")
public class TrainerSummaryDocument {

    @Id
    private String id;

    @Field("username")
    private String username;

    @Field("firstName")
    private String firstName;

    @Field("lastName")
    private String lastName;

    @Field("isActive")
    private boolean isActive;

    @Builder.Default
    @Field("years")
    private List<YearSummaryDocument> years = new ArrayList<>();
}