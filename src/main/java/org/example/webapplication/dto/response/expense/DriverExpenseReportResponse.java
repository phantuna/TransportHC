package org.example.webapplication.dto.response.expense;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverExpenseReportResponse {
    private String driverId;
    private String driverName;
    private int month;
    private int year;

    private Double totalExpense;
    private List<DriverExpenseItemResponse> items;
}
