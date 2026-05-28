package org.example.dao;

import org.example.dto.response.*;
import org.example.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for Trainer class
 */
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query("""
        select new org.example.dto.response.TrainerDTO(
                t.id,
                trainerUser.firstName,
                trainerUser.lastName,
                t.specialization.id,
                t.active
            )
        from Trainer t
        join t.user trainerUser
        where trainerUser.username = :username
    """)
    Optional<TrainerDTO> findTrainerDTOByUsername(@Param("username") String trainerUsername);


    @Query("select t from Trainer t where t.user.username = :trainerUsername")
    Optional<Trainer> findByUserUsername(@Param("trainerUsername") String trainerUsername);

    @Query("""
        select (count(t) > 0)
        from Trainer t
        join t.trainees tr
        where t.user.username = :trainerUsername
          and tr.user.username = :traineeUsername
    """)
    boolean existsTrainerTraineeRelation(@Param("trainerUsername") String trainerUsername,
                                         @Param("traineeUsername") String traineeUsername);

    boolean existsByUserUsername(@Param("username") String username);

    @Query("""
        select new org.example.dto.response.TrainerTrainingProfileView(
                t.name,
                t.date,
                t.type.id,
                t.duration,
                concat(traineeUser.firstName, ' ', traineeUser.lastName)
            )
        from Training t
            join t.trainee trainee
            join trainee.user traineeUser
        where t.trainer.user.username = :trainerUsername
          and (:#{#fromDate == null} = true or t.date >= cast(:fromDate as java.time.LocalDate))
          and (:#{#toDate == null} = true or t.date <= cast(:toDate as java.time.LocalDate))
          and (
                :#{#traineeName == null} = true
                or :traineeName = ''
                or lower(concat(traineeUser.firstName, ' ', traineeUser.lastName)) like lower(concat('%', :traineeName, '%'))
              )
          and (:#{#trainingTypeId == null} = true or t.type.id = :trainingTypeId)
        order by t.date desc, t.id desc
    """)
    List<TrainerTrainingProfileView> findTrainingsOfTrainerByCriteria(
            @Param("trainerUsername") String trainerUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeName") String traineeName,
            @Param("trainingTypeId") Integer trainingTypeId
    );

    @Query("""
        select distinct new org.example.dto.response.TraineeProfileTrainerDTO(
                trainerUser.username,
                trainerUser.firstName,
                trainerUser.lastName,
                trainer.specialization.id
            )
        from Trainer trainer
            join trainer.user trainerUser
        where
            trainer.active = true
            and not exists (
              select 1
              from Trainee tr
              join tr.trainers trn
              where tr.user.username = :traineeUsername
                  and trn.id = trainer.id
            )
    """)
    List<TraineeProfileTrainerDTO> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);

    List<Trainer> findAllByTraineesId(Long traineeId);

    @Query("""
        select distinct new org.example.dto.response.TraineeProfileTrainerDTO(
                trainerUser.username,
                trainerUser.firstName,
                trainerUser.lastName,
                trainer.specialization.id
            )
            from Trainer trainer
                join trainer.user trainerUser
            join trainer.trainees tt
                where tt.user.username = :traineeUsername
    """)
    List<TraineeProfileTrainerDTO> findTrainersOfTraineeByTraineeUsername(@Param("traineeUsername") String traineeUsername);


    @Query("""
        select distinct new org.example.dto.response.TrainerProfileTraineeDTO(
                trainee.user.username,
                trainee.user.firstName,
                trainee.user.lastName
            )
            from Trainee trainee
            join trainee.trainers tt
                where tt.user.username = :trainerUsername
    """)
    List<TrainerProfileTraineeDTO> findTraineesOfTrainerByTrainerUsername(@Param("trainerUsername") String trainerUsername);

    void deleteByUserUsername(String trainerUsername);

    @Query("""
        select t from Trainer t
        join t.user tu
        join t.specialization sp
        where tu.username in :trainerUsernames
    """)
    List<Trainer> findAllByUsernames(@Param("trainerUsernames") List<String> trainerUsernames);

}
