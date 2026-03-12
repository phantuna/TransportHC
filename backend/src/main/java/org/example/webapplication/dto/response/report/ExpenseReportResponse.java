package org.example.webapplication.dto.response.report;

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
public class ExpenseReportResponse {
    private String truckId;
    private String licensePlate;
    private double totalExpense;
    private List<ExpenseResponse> expenses;
    private String driverName;
    private LocalDate incurredDate;
}
