package org.example.webapplication.service;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.payroll.PayrollDeductionItem;
import org.example.webapplication.dto.response.payroll.PayrollDetailResponse;
import org.example.webapplication.dto.response.payroll.PayrollIncomeItem;
import org.example.webapplication.dto.response.payroll.PayrollTripItem;
import org.example.webapplication.entity.Payroll;
import org.example.webapplication.entity.User;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.expense.ExpenseRepository;
import org.example.webapplication.repository.payroll.PayrollRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollService {
    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final PayrollRepository payrollRepository;
    private final PermissionService permissionService;


    private List<PayrollDeductionItem> deductionByPayroll(Payroll payroll) {
        double baseSalary = payroll.getBaseSalary();

        return List.of(
                new PayrollDeductionItem(1, "BHXH bắt buộc (10.5%)", baseSalary * 0.105),
                new PayrollDeductionItem(2, "Kinh phí công đoàn (1%)", baseSalary * 0.01),
                new PayrollDeductionItem(3, "Tạm ứng tiền lương", payroll.getAdvanceSalary())
        );
    }

    private PayrollDetailResponse payrollSheetByMonth(Payroll payroll) {
        User driver = payroll.getUser();

        List<PayrollIncomeItem> incomes = List.of(
                new PayrollIncomeItem(
                        1,
                        "Lương căn bản",
                        payroll.getBaseSalary(),
                        payroll.getBaseSalary()
                )
        );

        List<PayrollTripItem> trips =
                payrollRepository.tripSalaryByDriverAndMonth(
                        driver.getId(),
                        LocalDate.of(payroll.getYear(), payroll.getMonth(), 1),
                        LocalDate.of(payroll.getYear(), payroll.getMonth(), 1)
                                .withDayOfMonth(
                                        LocalDate.of(
                                                payroll.getYear(),
                                                payroll.getMonth(),
                                                1
                                        ).lengthOfMonth()
                                )
                );

        double totalTripSalary =
                trips.stream().mapToDouble(PayrollTripItem::getAmount).sum();

        List<PayrollDeductionItem> deductions =
                deductionByPayroll(payroll);

        double totalIncome =
                payroll.getBaseSalary() + totalTripSalary;

        double totalDeduction =
                deductions.stream().mapToDouble(PayrollDeductionItem::getAmount).sum();

        return PayrollDetailResponse.builder()
                .driverId(driver.getId())
                .name(driver.getUsername())
                .month(payroll.getMonth())
                .year(payroll.getYear())
                .incomes(incomes)
                .trips(trips)
                .deductions(deductions)
                .totalTripSalary(totalTripSalary)
                .totalIncome(totalIncome)
                .totalDeduction(totalDeduction)
                .totalSalary(totalIncome - totalDeduction)
                .build();
    }

    @Transactional
    public Payroll ensurePayrollExists(User driver, int month, int year) {
        return payrollRepository
                .findByUser_IdAndMonthAndYear(driver.getId(), month, year)
                .orElseGet(() -> {
                    // 1. Tính lương chuyến
                    LocalDate from = LocalDate.of(year, month, 1);
                    LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

                    double tripSalary = payrollRepository
                            .tripSalaryByDriverAndMonth(driver.getId(), from, to)
                            .stream()
                            .mapToDouble(PayrollTripItem::getAmount)
                            .sum();

                    // 2. Tạo payroll mới
                    Payroll payroll = Payroll.builder()
                            .user(driver)
                            .month(month)
                            .year(year)
                            .baseSalary(driver.getBaseSalary())
                            .sheduleSalary(tripSalary)
                            .expenseSalary(tripSalary)
                            .advanceSalary(0.0)
                            .totalSalary(driver.getBaseSalary() + tripSalary)
                            .build();

                    return payrollRepository.save(payroll);
                });
    }

    @Cacheable(
            value = "payrolls_detail",
            key = "{#month, #year, T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()}"
    )
    public PayrollDetailResponse myPayrollByMonth(int month, int year) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.PAYROLL
        );

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));

        Payroll payroll = ensurePayrollExists(driver, month, year);

        return payrollSheetByMonth(payroll);
    }


    @Cacheable(value = "payrolls_list", key = "{#month, #year, #page, #size}")
    @Transactional
    public Page<PayrollDetailResponse> payrollAllByMonth(
            int month,
            int year,
            int page,
            int size
    ) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.PAYROLL
        );

        Pageable pageable = PageRequest.of(page, size);
        List<User> drivers = userRepository.findAllByRoles_Id("R_DRIVER");
        drivers.forEach(driver -> ensurePayrollExists(driver, month, year));

        List<Payroll> payrolls =
                payrollRepository.payrollByMonth(month, year, pageable);

        List<PayrollDetailResponse> responses =
                payrolls.stream()
                        .map(p -> payrollSheetByMonth(p))
                        .toList();

        long total = payrollRepository.countByMonthAndYear(month, year);

        return new PageImpl<>(responses, pageable, total);
    }

    @Caching(evict = {
            @CacheEvict(value = "payrolls_list", allEntries = true),
            @CacheEvict(value = "payrolls_detail", allEntries = true)
    })
    @Transactional
    public PayrollDetailResponse  updateBaseSalaryForMonth(
            String driverId,
            int month,
            int year,
            double newBaseSalary
    ) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.PAYROLL
        );

        Payroll payroll = payrollRepository
                .findByUser_IdAndMonthAndYear(driverId, month, year)
                .orElseThrow(() -> new AppException(ErrorCode.PAYROLL_NOT_FOUND));

        payroll.setBaseSalary(newBaseSalary);

        double totalIncome =
                newBaseSalary + payroll.getExpenseSalary();

        payroll.setTotalSalary(
                totalIncome - payroll.getAdvanceSalary()
        );

        payrollRepository.save(payroll);
        return payrollSheetByMonth(payroll);
    }

}