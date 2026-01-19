package org.example.webapplication.Dto.request.Truck;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruckExpenseRequest {

    private String truckId;

    @NotNull(message = "DATE_NOT_NULL")
    @PastOrPresent(message = "DATE_NOT_IN_FUTURE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @NotNull(message = "DATE_NOT_NULL")
    @PastOrPresent(message = "DATE_NOT_IN_FUTURE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;
}
