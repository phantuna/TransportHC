package org.example.webapplication.repository.payroll;

import org.example.webapplication.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PayrollRepositoryCustom {
    Double sumApproedExpenseByDriverAndMonth (User driver, LocalDate from, LocalDate to);
}
