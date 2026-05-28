package org.example.dao;

import org.example.dto.response.TraineeDTO;
import org.example.dto.response.TraineeTrainingProfileView;
import org.example.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for Trainee class
 */
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUserUsername(String username);

    @Query("""
        select new org.example.dto.response.TraineeDTO(
               t.id,
               t.user.firstName,
               t.user.lastName,
               t.address,
               t.dateOfBirth,
               t.active
            )
            from Trainee t
            where t.user.username = :username
                  and t.active = true
    """)
    Optional<TraineeDTO> findTraineeDTOByUsername(@Param("username") String traineeUsername);

    boolean existsByUserUsername(@Param("username") String username);

    @Query("""
        select new org.example.dto.response.TraineeTrainingProfileView(
                t.name,
                t.date,
                t.type.id,
                t.duration,
                concat(trainerUser.firstName, ' ', trainerUser.lastName)
            )
        from Training t
            join t.trainer trainer
            join trainer.user trainerUser
        where t.trainee.user.username = :traineeUsername
          and (:#{#fromDate == null} = true or t.date >= cast(:fromDate as java.time.LocalDate))
          and (:#{#toDate == null} = true or t.date <= cast(:toDate as java.time.LocalDate))
          and (
                :#{#trainerName == null} = true
                or :trainerName = ''
                or lower(concat(trainerUser.firstName, ' ', trainerUser.lastName)) like lower(concat('%', :trainerName, '%'))
              )
          and (:#{#trainingTypeId == null} = true or t.type.id = :trainingTypeId)
        order by t.date desc, t.id desc
    """)
    List<TraineeTrainingProfileView> findTrainingsOfTraineeByCriteria(
            @Param("traineeUsername") String traineeUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trainerName") String trainerName,
            @Param("trainingTypeId") Integer trainingTypeId
    );

    void deleteByUserUsername(String username);

}
