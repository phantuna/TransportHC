package org.example.webapplication.dto.response.travel;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TravelDailyReportResponse {
    private String truckId;
    private int month;
    private int year;
    private long totalTrip;
    private List<TravelDailyReportItemResponse> items;
}
