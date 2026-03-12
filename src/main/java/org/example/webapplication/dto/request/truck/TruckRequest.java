package org.example.webapplication.dto.request.truck;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.TruckStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckRequest {
    private String driverId;
    @NotBlank(message = "validation.truck_type.required")
    private String typeTruck;

    @NotBlank(message = "validation.license_plate.required")
    @Pattern(
            regexp = "^[0-9A-Z\\-\\.]+$",
            message = "validation.license_plate.invalid"
    )
    private String licensePlate;

    @NotNull(message = "validation.ganmooc.required")
    private boolean ganMooc;

    @NotNull(message = "validation.truck_status.required")
    private TruckStatus status;

}
