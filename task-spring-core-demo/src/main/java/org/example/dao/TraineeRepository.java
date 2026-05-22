package org.example.dao;

import jakarta.validation.constraints.NotBlank;
import org.example.dao.projection.TraineeView;
import org.example.dao.projection.TrainingView;
import org.example.dto.response.TraineeDTO;
import org.example.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for User class
 */
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserUsername(String username);

    @Query("""
        select t.id as id,
                   t.user.id as userId,
                   t.user.firstName as firstName,
                   t.user.lastName as lastName,
                   t.address as address,
                   t.active as isActive,
                   t.dateOfBirth as dateOfBirth,
                   t.createdAt as createdAt,
                   t.updatedAt as updatedAt
            from Trainee t
    """)
    List<TraineeView> findAllTraineesView();


    @Query("""
        select t.id as id,
                   t.user.id as userId,
                   t.user.firstName as firstName,
                   t.user.lastName as lastName,
                   t.address as address,
                   t.active as isActive,
                   t.dateOfBirth as dateOfBirth,
                   t.createdAt as createdAt,
                   t.updatedAt as updatedAt
            from Trainee t
            where t.id = :traineeId
    """)
    Optional<TraineeView> findTraineeViewById(@Param("traineeId") Long traineeId);

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
        where t.trainee.user.username = :traineeUsername
          and (:#{#fromDate == null} = true or t.date >= cast(:fromDate as java.time.LocalDate))
          and (:#{#toDate == null} = true or t.date <= cast(:toDate as java.time.LocalDate))
          and (
                :#{#trainerName == null} = true
                or :trainerName = ''
                or lower(concat(t.trainer.user.firstName, ' ', t.trainer.user.lastName)) like lower(concat('%', :trainerName, '%'))
              )
          and (:#{#trainingTypeId == null} = true or t.type.id = :trainingTypeId)
        order by t.date desc, t.id desc
    """)
    List<TrainingView> findTrainingsOfTraineeByCriteria(
            @Param("traineeUsername") String traineeUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trainerName") String trainerName,
            @Param("trainingTypeId") Integer trainingTypeId
    );

}
