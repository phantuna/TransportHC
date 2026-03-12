package org.example.webapplication.dto.response.travel;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelDailyReportItemResponse {
    private int stt;
    private String driverCode;
    private String driverName;
    private String licensePlate;
    private Boolean GanMooc; // mooc
    private LocalDate date;
    private String startPlace;
    private String endPlace;
    private Long tripCount;
}
