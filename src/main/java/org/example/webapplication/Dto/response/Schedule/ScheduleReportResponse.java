package org.example.webapplication.Dto.response.Schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.Dto.response.Travel.TravelScheduleResponse;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleReportResponse {
    private String truckId;
    private String licensePlate;

    private List<TravelScheduleResponse> travels;

    private double grandTotalExpense;

}
