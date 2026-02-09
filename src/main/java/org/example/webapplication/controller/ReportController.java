package org.example.webapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.truck.TruckExpenseRequest;
import org.example.webapplication.dto.response.expense.DriverExpenseReportResponse;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.dto.response.report.ExpenseReportDetailResponse;
import org.example.webapplication.dto.response.report.ExpenseReportResponse;
import org.example.webapplication.dto.response.payroll.PayrollDetailResponse;
import org.example.webapplication.dto.response.report.ExpenseSummaryResponse;
import org.example.webapplication.dto.response.schedule.ScheduleReportResponse;
import org.example.webapplication.dto.response.travel.TravelDailyReportResponse;
import org.example.webapplication.service.PayrollService;
import org.example.webapplication.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {
    private final ReportService reportService;
    private final PayrollService  payrollService;

    @GetMapping("/payroll/my")
    @PreAuthorize("isAuthenticated()")
    public PayrollDetailResponse myPayrollByMonth(
            @RequestParam int month,
            @RequestParam int year
    ) {
        return payrollService.myPayrollByMonth(month, year);
    }

    // Manager / Accountant xem lương TẤT CẢ driver theo tháng
    @GetMapping("/payroll/all")
    @PreAuthorize("isAuthenticated()")
    public Page<PayrollDetailResponse> payrollAllByMonth(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return payrollService.payrollAllByMonth(month, year,page,size);
    }
    @PutMapping("/payroll/{driverId}")
    @PreAuthorize("isAuthenticated()")
    public PayrollDetailResponse  updateBaseSalary(
            @PathVariable String driverId,
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam double amount
    ) {
        return payrollService.updateBaseSalaryForMonth(
                driverId, month, year, amount
        );
    }

    @GetMapping("/allTruckExpense")
    @PreAuthorize("isAuthenticated()")
    public Page<ExpenseSummaryResponse> allTruckExpenseSummaryReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return reportService.allTruckExpenseSummaryReport(page, size);
    }

    @GetMapping("/truckExpenseDetail/{truckId}")
    @PreAuthorize("isAuthenticated()")
    public Page<ExpenseReportDetailResponse> detail(
            @PathVariable String truckId,
            @RequestParam(required = false)
            LocalDate from,
            @RequestParam(required = false)
            LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return reportService.truckExpenseDetail(truckId, from, to,page,size);
    }


    @GetMapping("/scheduleReport/{truckId}")
    @PreAuthorize("isAuthenticated()")
    public ScheduleReportResponse ScheduleReport(
            @NotBlank @PathVariable String truckId){
        return reportService.scheduleReport(truckId);
    }

    @GetMapping("/driver-expense")
    @PreAuthorize("isAuthenticated()")
    public DriverExpenseReportResponse driverExpenseReport(
            @RequestParam String driverId,
            @RequestParam int month,
            @RequestParam int year
    ) {
        return reportService.driverExpenseReport(driverId, month, year);
    }
    @GetMapping("/travel-daily")
    @PreAuthorize("isAuthenticated()")
    public TravelDailyReportResponse travelDailyReport(
            @RequestParam(required = false) String truckId,
            @RequestParam int month,
            @RequestParam int year
    ) {
        return reportService.travelDailyReport(truckId, month, year);
    }

}
