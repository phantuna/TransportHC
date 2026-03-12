package org.example.webapplication.service.mapper;

import org.example.webapplication.dto.response.truck.TruckResponse;
import org.example.webapplication.entity.Truck;
import org.example.webapplication.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TruckMapper {

    public TruckResponse toResponse(Truck truck) {
        User driver = truck.getDriver();

            return TruckResponse.builder()
                    .id(truck.getId())
                    .typeTruck(truck.getTypeTruck())
                    .licensePlate(truck.getLicensePlate())
                    .ganMooc(truck.isGanMooc())
                    .status(truck.getStatus())
                    .driverId(driver != null ? driver.getId() : null)
                    .driverName(driver != null ? driver.getUsername() : null)
                    .build();
        }
}


