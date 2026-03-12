package org.example.webapplication.dto.response.travel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelScheduleReportResponse {
    private String travelId;
    private String truckPlate;
    private String driverName;
    private String startPlace;
    private String endPlace;
    private LocalDate startDate;
    private LocalDate endDate;
    private String scheduleId;
    private Double totalExpense;
}
