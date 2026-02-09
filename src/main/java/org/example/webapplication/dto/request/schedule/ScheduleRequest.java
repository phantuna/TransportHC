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
    @NotBlank(message = "validation.place.not_null")
    private String startPlace;
    @NotBlank(message = "validation.place.not_null")
    private String endPlace;
    @Positive(message = " validation.expense.gt_zero")
    private double expense;

    @Size(max = 300, message = "validation.description.exceed_300")
    private String description;

}
