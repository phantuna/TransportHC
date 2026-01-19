package org.example.webapplication.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayrollDetailResponse {
    private String driverId;
    private String name;
    private int month;
    private int year;
    private double baseSalary;
    private double expenseSalary;
    private double advanceSalary;
    private double totalSalary;
}
