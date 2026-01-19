package org.example.webapplication.Dto.request.Expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.Enum.TypeExpense;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseRequest {
    @NotNull (message = "TYPE_EXPENSE_NOT_NULL")
    private TypeExpense type;

    @Positive(message = "EXPENSE_GREATER_THAN_ZERO")
    private double expense;

    @Size(max = 300, message = "DESCRIPTION_NOT_EXCEED_300")
    private String description;

    @NotBlank(message = "TRAVEL_ID_NOT_NULL")
    private String travelId;

    @NotNull(message = "DATE_NOT_NULL")
    @PastOrPresent(message = "DATE_NOT_IN_FUTURE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate incurredDate;



}
