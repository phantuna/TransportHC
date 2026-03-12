package org.example.webapplication.dto.response.payroll;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollDeductionItem {
    private int order;
    private String name;
    private double amount;
}
