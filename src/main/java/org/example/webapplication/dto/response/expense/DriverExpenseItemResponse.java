package org.example.webapplication.dto.response.expense;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.TypeExpense;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverExpenseItemResponse  {
    private int order;
    private TypeExpense type;
    private Double amount;
}
