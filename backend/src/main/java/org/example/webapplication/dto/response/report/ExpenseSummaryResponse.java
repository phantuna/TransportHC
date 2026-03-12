package org.example.webapplication.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseSummaryResponse {
    private String truckId;
    private String licensePlate;
    private String driverName;
    private double totalExpense;
}
