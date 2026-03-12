package org.example.webapplication.dto.request.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.TypeExpense;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseRequest {
    @NotNull (message = "validation.expense_type.not_null")
    private TypeExpense type;

    @Positive(message = "validation.expense.gt_zero")
    private double expense;

    @Size(max = 300, message = "validation.description.exceed_300")
    private String description;

    @NotBlank(message = "validation.travel_id.not_null")
    private String travelId;

    @NotNull(message = "validation.date.not_null")
    @PastOrPresent(message = "validation.date.not_in_future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate incurredDate;



}
