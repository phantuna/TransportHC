package org.example.webapplication.dto.response.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollIncomeItem {
    private int order;
    private String name;
    private Double baseAmount;
    private Double actualAmount;
}
