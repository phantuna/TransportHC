package org.example.webapplication.repository.expense;

import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.entity.Expense;
import org.example.webapplication.enums.TypeExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepositoryCustom {
    Page<ExpenseResponse> findExpenseReport(
            String truckId,
            LocalDate fromDate,
            LocalDate toDate,
            String driverName,
            Pageable pageable
    );

    Double sumApprovedExpense(
            String truckId,
            LocalDate fromDate,
            LocalDate toDate
    );


}
