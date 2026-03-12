package org.example.webapplication.dto.request.payroll;

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
    @NotBlank(message = "validation.driver_id.not_null")
    private String driverId ;

    @NotNull(message = "validation.date.not_null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;

    @NotNull(message = "validation.date.not_null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;

    @PositiveOrZero(message = "validation.advance.gt_zero")
    private Double advance;
}
