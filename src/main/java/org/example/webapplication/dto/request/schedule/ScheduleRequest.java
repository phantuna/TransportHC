package org.example.webapplication.dto.request.schedule;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRequest {
    @NotBlank(message = "PLACE_NOT_NULL")
    private String startPlace;
    @NotBlank(message = "PLACE_NOT_NULL")
    private String endPlace;
    @Positive(message = " EXPENSE_GREATER_THAN_ZERO")
    private double expense;

    @Size(max = 300, message = "DESCRIPTION_NOT_EXCEED_300")
    private String description;

}
