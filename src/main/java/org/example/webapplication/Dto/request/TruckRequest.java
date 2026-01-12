package org.example.webapplication.Dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.Enum.TruckStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TruckRequest {
    private String driverId;
    @NotBlank(message = "TYPE_TRUCK_REQUIRED")
    private String typeTruck;

    @NotBlank(message = "LICENSE_PLATE_REQUIRED")
    @Pattern(
            regexp = "^[0-9A-Z\\-\\.]+$",
            message = "LICENSE_PLATE_INVALID"
    )
    private String licensePlate;

    @NotNull(message = "GANMOOC_REQUIRED")
    private boolean ganMooc;

    @NotNull(message = "TRUCK_STATUS_REQUIRED")
    private TruckStatus status;

}
