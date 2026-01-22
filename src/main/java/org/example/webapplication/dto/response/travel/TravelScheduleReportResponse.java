package org.example.webapplication.dto.response.travel;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TravelScheduleReportResponse {
    String travelId;
    LocalDate startDate;
    LocalDate endDate;
    String startPlace;
    String endPlace;
    Double totalExpense;
}
