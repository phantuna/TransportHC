package org.example.webapplication.dto.response.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayrollResponse {
    private double baseSalary;
    private double sheduleSalary;
    private double expenseSalary;
    private double advanceSalary;
    private double totalSalary;
}
