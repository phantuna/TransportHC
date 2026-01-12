package org.example.webapplication.Dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PayrollRequest {
    @NotBlank(message = "DRIVER_ID_NOT_NULL")
    private String driverId ;

    @NotNull(message = "DATE_NOT_NULL")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;

    @NotNull(message = "DATE_NOT_NULL")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;

    @PositiveOrZero(message = "ADVANCE_GREATER_THAN_ZERO")
    private Double advance;
}
