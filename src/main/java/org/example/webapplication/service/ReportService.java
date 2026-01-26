package org.example.webapplication.service;

import org.example.webapplication.dto.request.truck.TruckExpenseRequest;
import org.example.webapplication.dto.response.report.ExpenseReportDetailResponse;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.dto.response.payroll.PayrollDetailResponse;
import org.example.webapplication.dto.response.report.ExpenseSummaryResponse;
import org.example.webapplication.dto.response.schedule.ScheduleDocumentResponse;
import org.example.webapplication.dto.response.schedule.ScheduleReportResponse;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.repository.expense.ExpenseRepository;
import org.example.webapplication.repository.payroll.PayrollRepository;
import org.example.webapplication.repository.schedule.ScheduleRepository;
import org.example.webapplication.repository.travel.TravelRepository;
import org.example.webapplication.repository.truck.TruckRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.enums.ApprovalStatus;
import org.example.webapplication.dto.response.travel.TravelScheduleResponse;
import org.example.webapplication.entity.*;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageImpl;


import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final PayrollRepository payrollRepository;
    private final TruckRepository truckRepository;
    private final PermissionService permissionService;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepository;



    private PayrollDetailResponse buildPayrollDetail(
            User driver,
            int month,
            int year
    ) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        double expenseSalary = Optional.ofNullable(
                payrollRepository.sumApproedExpenseByDriverAndMonth(driver, from, to)
        ).orElse(0.0);

        double advance = payrollRepository.findByUser(driver)
                .map(Payroll::getAdvanceSalary)
                .orElse(0.0);

        double total = driver.getBaseSalary() + expenseSalary - advance;

        return PayrollDetailResponse.builder()
                .driverId(driver.getId())
                .name(driver.getUsername())
                .month(month)
                .year(year)
                .baseSalary(driver.getBaseSalary())
                .expenseSalary(expenseSalary)
                .advanceSalary(advance)
                .totalSalary(total)
                .build();
    }

    public PayrollDetailResponse myPayrollByMonth(int month, int year) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.REPORT
        );
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));
        return buildPayrollDetail(driver, month, year);
    }

    @Transactional
    public Page<PayrollDetailResponse> payrollAllByMonth(
            int month,
            int year,
            int page,
            int size
    ) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.REPORT
        );
        Pageable pageable = PageRequest.of(page, size);
        List<PayrollDetailResponse> data =
                payrollRepository.payrollByMonth(month, year, pageable);

        long total = userRepository.countByRoles_Id("R_DRIVER");

        return new PageImpl<>(data, pageable, total);
    }


    public Page<ExpenseSummaryResponse> allTruckExpenseSummaryReport(
            int page,
            int size
    ) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.REPORT
        );
        Pageable pageable = PageRequest.of(page, size);
        return truckRepository.getAllTruckExpenseSummary(pageable);
    }


    public Page<ExpenseReportDetailResponse> truckExpenseDetail(
            String truckId,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.REPORT
        );
        Pageable pageable = PageRequest.of(page, size);
        return truckRepository.getExpenseDetailsByTruck(
                truckId, from, to, pageable
        );
    }

    public ScheduleReportResponse scheduleReport(String truckId) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.REPORT
        );
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new AppException(ErrorCode.TRUCK_NOT_FOUND));

        List<TravelScheduleReportResponse> travelSummaries = scheduleRepository.getTravelScheduleReport(truckId);
        List<ExpenseResponse> expenses = scheduleRepository.findExpensesByTravel(truckId);
        List<ScheduleDocumentResponse> documents = scheduleRepository.findDocumentsByTravel(truckId);

        Map<String, List<ExpenseResponse>> expenseMap =
                expenses.stream()
                        .collect(Collectors.groupingBy(ExpenseResponse::getTravelId));

        Map<String, List<ScheduleDocumentResponse>> documentMap =
                documents.stream()
                        .collect(Collectors.groupingBy(ScheduleDocumentResponse::getScheduleId));

        List<TravelScheduleResponse> travelResponses = new ArrayList<>();
        double grandTotal = 0;

        for (TravelScheduleReportResponse summary : travelSummaries) {
            String travelId = summary.getTravelId();
            List<ExpenseResponse> expenseResponses = expenseMap.getOrDefault(travelId, List.of());
            List<ScheduleDocumentResponse> documentResponses = documentMap.getOrDefault(summary.getScheduleId(), List.of());

            double travelTotal = summary.getTotalExpense();
            grandTotal += travelTotal;

            travelResponses.add(
                    TravelScheduleResponse.builder()
                            .travelId(travelId)
                            .scheduleName(
                                    summary.getStartPlace() == null
                                            ? "No schedule"
                                            : summary.getStartPlace() + " - " + summary.getEndPlace()
                            )
                            .startDate(summary.getStartDate())
                            .endDate(summary.getEndDate())
                            .expense(expenseResponses)
                            .document(documentResponses)
                            .totalExpense(travelTotal)
                            .build()
            );
        }

        return ScheduleReportResponse.builder()
                .truckId(truck.getId())
                .licensePlate(truck.getLicensePlate())
                .travels(travelResponses)
                .grandTotalExpense(grandTotal)
                .build();
    }
}
