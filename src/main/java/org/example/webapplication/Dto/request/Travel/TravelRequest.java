package org.example.webapplication.Dto.request.Travel;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelRequest {
    @NotBlank(message = "TRUCK_ID_NOT_NULL")
    private String truckId;

    @NotBlank(message = "SCHEDULE_ID_NOT_NULL")
    private String scheduleId;


    @NotNull(message = "DATE_NOT_NULL")
    @FutureOrPresent(message = "DATE_MUST_BE_TODAY_OR_FUTURE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "DATE_NOT_NULL")
    @FutureOrPresent(message = "DATE_MUST_BE_TODAY_OR_FUTURE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
