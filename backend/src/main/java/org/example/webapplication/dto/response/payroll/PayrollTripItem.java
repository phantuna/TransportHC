package org.example.webapplication.dto.response.payroll;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollTripItem {
    private int order;
    private String routeName;
    private int tripCount;
    private double unitPrice;
    private double amount;
}
