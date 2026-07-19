package org.example.workload.repository;

import org.example.workload.document.TrainerSummaryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerSummaryRepository extends MongoRepository<TrainerSummaryDocument, String> {

    Optional<TrainerSummaryDocument> findByUsername(String username);

    List<TrainerSummaryDocument> findByFirstNameAndLastName(String firstName, String lastName);
}