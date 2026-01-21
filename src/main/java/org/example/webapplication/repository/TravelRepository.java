package org.example.webapplication.repository;

import org.example.webapplication.entity.Travel;
import org.example.webapplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TravelRepository extends JpaRepository<Travel,String> {

    List<Travel> findByTruck_Id(String truckId);

    @Query("""
        SELECT t 
        FROM Travel t
        WHERE t.truck.id = :truckId
             AND t.startDate BETWEEN :fromDate AND :toDate
    """)
    List<Travel> findByTruck_IdAndStartDateBetween(String truckId,
                                                   LocalDate fromDate,
                                                   LocalDate toDate);

    @Query("""
        SELECT t
        FROM Travel t
            Where t.user =:user
                AND t.startDate BETWEEN :startDate AND :startDate2
    """)
    List<Travel> findByUserAndStartDateBetween(@Param("user")User user,
                                               @Param("startDate")LocalDate startDate,
                                               @Param("startDate2")LocalDate startDate2);
    boolean existsByTruck_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String truckId,
                                                                                LocalDate endDate,
                                                                             LocalDate startDate);

    @Query("""
        SELECT COUNT(t) > 0
        FROM Travel t
        WHERE t.truck.id = :truckId
          AND t.startDate = :startDate
    """)
    boolean existsByTruck_IdAndStartDate(String truckId, LocalDate startDate);


    @Query("""
        SELECT COUNT(t) > 0
        FROM Travel t
        WHERE t.truck.id = :truckId
          AND t.startDate = :startDate
          AND t.id <> :travelId
    """)
    boolean existsTravel(
            String truckId,
            LocalDate startDate,
            String travelId
    );



    @Query("""
        SELECT COUNT(t) > 0
        FROM Travel t
        WHERE t.truck.id = :truckId
          AND t.startDate <= CURRENT_DATE
          AND (t.endDate IS NULL OR t.endDate >= CURRENT_DATE)
    """)
    boolean existsActiveTravelToday(@Param("truckId") String truckId);
}
