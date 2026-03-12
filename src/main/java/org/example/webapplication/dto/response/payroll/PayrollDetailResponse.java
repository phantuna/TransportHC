package org.example.webapplication.dto.response.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayrollDetailResponse {
    private String driverId;
    private String name;
    private int month;
    private int year;

    private List<PayrollIncomeItem> incomes;      // BẢNG 1
    private List<PayrollTripItem> trips;           // BẢNG 2
    private List<PayrollDeductionItem> deductions; // BẢNG 3

    private double totalTripSalary;
    private double totalIncome;     // bảng 1 + bảng 2
    private double totalDeduction;  // bảng 3
    private double totalSalary;
}
