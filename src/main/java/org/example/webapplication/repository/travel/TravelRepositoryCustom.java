package org.example.webapplication.repository.travel;

import org.example.webapplication.entity.Travel;
import org.example.webapplication.entity.User;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TravelRepositoryCustom {
    List<Travel> findByTruck_IdAndStartDateBetween(String truckId, LocalDate fromDate, LocalDate toDate);

    List<Travel> findByUserAndStartDateBetween(User user, LocalDate startDate, LocalDate startDate2);

    boolean existsByTruck_IdAndStartDate(String truckId, LocalDate startDate);

    boolean existsTravel(String truckId, LocalDate startDate, String travelId
    );
    boolean existsActiveTravelToday(String truckId);


}