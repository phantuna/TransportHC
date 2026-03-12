package org.example.webapplication.repository.payroll;

import com.querydsl.core.types.dsl.NumberExpression;
import org.example.webapplication.dto.response.payroll.PayrollDetailResponse;
import org.example.webapplication.dto.response.payroll.PayrollTripItem;
import org.example.webapplication.entity.Payroll;
import org.example.webapplication.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PayrollRepositoryCustom {
    List<Payroll> payrollByMonth(
            int month,
            int year,
            Pageable pageable
    );
    Double sumApproedExpenseByDriverAndMonth(
            User driver,
            LocalDate from,
            LocalDate to
    );
    List<PayrollTripItem> tripSalaryByDriverAndMonth(
            String driverId,
            LocalDate from,
            LocalDate to
    );
}
