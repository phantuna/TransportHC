package org.example.webapplication.dto.response.travel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.dto.response.expense.ExpenseResponse;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TravelResponse {
    private String travelId;

    private String truckPlate;
    private String driverName;

    private String scheduleName;

    private LocalDate startDate;
    private LocalDate endDate;
    private List<ExpenseResponse> expenses;
    private double totalExpense;
}
