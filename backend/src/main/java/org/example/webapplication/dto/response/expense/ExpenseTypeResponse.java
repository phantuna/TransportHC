package org.example.webapplication.dto.response.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.TypeExpense;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseTypeResponse {
    private String id;
    private TypeExpense key;
    private String label;
    private String description;

}