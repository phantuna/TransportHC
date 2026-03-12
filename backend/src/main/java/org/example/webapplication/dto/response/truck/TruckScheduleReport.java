package org.example.webapplication.dto.response.truck;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckScheduleReport {
    private String id;
    private String drivername;
    private String licensePlate;
    private boolean ganMooc;
    private String createdDate;

    private String startPlace;
    private String endPlace;

    private int totalSchedule;
}
