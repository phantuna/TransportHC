package org.example.webapplication.repository.travel;

import org.example.webapplication.dto.response.travel.TravelDailyReportItemResponse;
import org.example.webapplication.dto.response.travel.TravelResponse;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
import org.example.webapplication.entity.Travel;
import org.example.webapplication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TravelRepositoryCustom {
    boolean existsByTruck_IdAndStartDate(String truckId, LocalDate startDate);

    boolean existsTravel(String truckId, LocalDate startDate, String travelId
    );
    boolean existsActiveTravelToday(String truckId);
    Page<TravelScheduleReportResponse> findTravelPage(Pageable pageable);
    Travel findCurrentBySchedule(String scheduleId, LocalDate today);
    List<TravelDailyReportItemResponse> dailyTravelReport(
            String truckId,
            Integer month,
            Integer year
    );
    Page<Travel> findTravelPageWithFetch(Pageable pageable);

}