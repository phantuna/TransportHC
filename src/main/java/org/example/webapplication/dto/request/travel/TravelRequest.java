package org.example.webapplication.dto.request.travel;

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
    @NotBlank(message = "validation.truck_id.not_null")
    private String truckId;

    @NotBlank(message = "validation.schedule_id.not_null")
    private String scheduleId;


    @NotNull(message = "validation.date.not_null")
    @FutureOrPresent(message = "validation.date.today_or_future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "validation.date.not_null")
    @FutureOrPresent(message = "validation.date.today_or_future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
