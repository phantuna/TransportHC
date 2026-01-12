package org.example.webapplication.Dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.Enum.TruckStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckResponse {
    private String id;
    private String typeTruck;
    private String licensePlate; // bien so xe
    private boolean ganMooc;

    private TruckStatus status;
    private String driverId;
    private String driverName;
}
