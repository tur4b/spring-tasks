package org.example.dao;

import org.example.entity.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for TrainingType class
 */
public interface TrainingTypeRepository extends JpaRepository<TrainingType, Integer> {

}
