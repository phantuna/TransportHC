package org.example.webapplication.dto.request.truck;

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

    @NotNull(message = "validation.date.not_null")
    @PastOrPresent(message = "validation.date.not_in_future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @NotNull(message = "validation.date.not_null")
    @PastOrPresent(message = "validation.date.not_in_future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;
}
