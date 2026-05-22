package org.example.dao;

import org.example.dao.projection.TrainingView;
import org.example.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("""
        select t.id as id,
            t.name as name,
            t.trainer.id as trainerId,
            t.trainee.id as traineeId,
            t.type.id as typeId,
            t.type.name as typeName,
            t.date as date,
            t.duration as duration
            from Training t
    """)
    List<TrainingView> findAllTrainingsView();

    @Query("""
        select t.id as id,
            t.name as name,
            t.trainer.id as trainerId,
            t.trainee.id as traineeId,
            t.type.id as typeId,
            t.type.name as typeName,
            t.date as date,
            t.duration as duration
            from Training t
            where t.id = :trainingId
    """)
    Optional<TrainingView> findTrainingViewById(@Param("trainingId") Long trainingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Training t set t.active = false where t.id = :trainingId")
    int softDeleteById(@Param("trainingId") Long trainingId);

}
