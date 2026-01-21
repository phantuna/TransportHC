package org.example.webapplication.repository.expense;

import org.example.webapplication.entity.Expense;
import org.example.webapplication.enums.TypeExpense;

import java.util.List;

public interface ExpenseRepositoryCustom {
    List<Expense> searchExpenses(
            String keyword,
            TypeExpense type,
            Boolean deleted,
            int page,
            int size
    );


}
