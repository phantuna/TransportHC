package org.example.webapplication.Repository;

import org.example.webapplication.Entity.Travel;
import org.example.webapplication.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TravelRepository extends JpaRepository<Travel,String> {

    List<Travel> findByTruck_Id(String truckId);
    List<Travel> findByTruck_IdAndStartDateBetween(String truckId,
                                                   LocalDate fromDate,
                                                   LocalDate toDate);
    List<Travel> findByUserAndStartDateBetween(User user,
                                               LocalDate startDate,
                                               LocalDate startDate2);
    boolean existsByTruck_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String truckId,
                                                                                LocalDate endDate,
                                                                                LocalDate startDate);
    boolean existsByTruck_IdAndStartDate(String truckId, LocalDate startDate);


    boolean existsByTruck_IdAndStartDateAndIdNot(
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
