package org.example.dao;

import org.example.dao.projection.TrainerView;
import org.example.dao.projection.TrainingView;
import org.example.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query("""
        select t.id as id,
                   t.user.id as userId,
                   t.user.firstName as firstName,
                   t.user.lastName as lastName,
                   t.specialization.id as specializationId,
                   t.specialization.name as specializationName,
                   t.active as isActive,
                   t.createdAt as createdAt,
                   t.updatedAt as updatedAt
            from Trainer t
    """)
    List<TrainerView> findAllTrainersView();


    @Query("""
        select t.id as id,
                   t.user.id as userId,
                   t.user.firstName as firstName,
                   t.user.lastName as lastName,
                   t.specialization.id as specializationId,
                   t.specialization.name as specializationName,
                   t.active as isActive,
                   t.createdAt as createdAt,
                   t.updatedAt as updatedAt
            from Trainer t
            where t.id = :trainerId
    """)
    Optional<TrainerView> findTrainerViewById(@Param("trainerId") Long trainerId);

    @Query("select t from Trainer t where t.user.username = :trainerUsername")
    Optional<Trainer> findByUserUsername(@Param("trainerUsername") String trainerUsername);

    @Query("""
        select (count(t) > 0)
        from Trainer t
        join t.trainees tr
        where t.id = :trainerId
          and tr.id = :traineeId
    """)
    boolean existsTrainerTraineeRelation(@Param("trainerId") Long trainerId,
                                         @Param("traineeId") Long traineeId);

    boolean existsByUserUsername(@Param("username") String username);

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
        where t.trainer.user.username = :trainerUsername
          and (:#{#fromDate == null} = true or t.date >= cast(:fromDate as java.time.LocalDate))
          and (:#{#toDate == null} = true or t.date <= cast(:toDate as java.time.LocalDate))
          and (
                :#{#traineeName == null} = true
                or :traineeName = ''
                or lower(concat(t.trainee.user.firstName, ' ', t.trainee.user.lastName)) like lower(concat('%', :traineeName, '%'))
              )
          and (:#{#trainingTypeId == null} = true or t.type.id = :trainingTypeId)
        order by t.date desc, t.id desc
    """)
    List<TrainingView> findTrainingsOfTrainerByCriteria(
            @Param("trainerUsername") String trainerUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeName") String traineeName,
            @Param("trainingTypeId") Integer trainingTypeId
    );

    @Query("""
        select t.id as id,
               t.user.id as userId,
               t.user.firstName as firstName,
               t.user.lastName as lastName,
               t.specialization.id as specializationId,
               t.specialization.name as specializationName,
               t.active as isActive,
               t.createdAt as createdAt,
               t.updatedAt as updatedAt
        from Trainer t
        where not exists (
              select 1
              from Trainee tr
              join tr.trainers trn
              where tr.user.username = :traineeUsername
                and trn.id = t.id
          )
    """)
    List<TrainerView> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);

    List<Trainer> findAllByTraineesId(Long traineeId);
}
