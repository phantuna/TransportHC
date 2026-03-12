package org.example.webapplication.dto.request.expense;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.TypeExpense;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseTypeRequest {
    private TypeExpense key;
    private String label;
    private String description;
}