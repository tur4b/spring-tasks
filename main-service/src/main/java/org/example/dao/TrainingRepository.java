package org.example.dao;

import org.example.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA repository for Trainingg class
 */
public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Training t set t.active = false where t.id = :trainingId")
    int softDeleteById(@Param("trainingId") Long trainingId);

}
