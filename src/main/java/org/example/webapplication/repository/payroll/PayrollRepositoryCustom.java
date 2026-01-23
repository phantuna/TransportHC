package org.example.webapplication.repository.payroll;

import com.querydsl.core.types.dsl.NumberExpression;
import org.example.webapplication.dto.response.payroll.PayrollDetailResponse;
import org.example.webapplication.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PayrollRepositoryCustom {
    List<PayrollDetailResponse> payrollByMonth(
            int month,
            int year,
            Pageable pageable
    );
    Double sumApproedExpenseByDriverAndMonth(
            User driver,
            LocalDate from,
            LocalDate to
    );
}
