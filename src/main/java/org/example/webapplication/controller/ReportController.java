package org.example.webapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.truck.TruckExpenseRequest;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.dto.response.report.ExpenseReportDetailResponse;
import org.example.webapplication.dto.response.report.ExpenseReportResponse;
import org.example.webapplication.dto.response.payroll.PayrollDetailResponse;
import org.example.webapplication.dto.response.report.ExpenseSummaryResponse;
import org.example.webapplication.dto.response.schedule.ScheduleReportResponse;
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

    @GetMapping("/payroll/my")
    @PreAuthorize("hasAuthority('VIEW_REPORT')")
    public PayrollDetailResponse myPayrollByMonth(
            @RequestParam int month,
            @RequestParam int year
    ) {
        return reportService.myPayrollByMonth(month, year);
    }

    // Manager / Accountant xem lương TẤT CẢ driver theo tháng
    @GetMapping("/payroll/all")
    @PreAuthorize("hasAuthority('MANAGE_REPORT')")
    public Page<PayrollDetailResponse> payrollAllByMonth(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return reportService.payrollAllByMonth(month, year,page,size);
    }

    @GetMapping("/allTruckExpense")
    public Page<ExpenseSummaryResponse> allTruckExpenseSummaryReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return reportService.allTruckExpenseSummaryReport(page, size);
    }

    @GetMapping("/truckExpenseDetail/{truckId}")
    @PreAuthorize("hasAuthority('VIEW_REPORT')")
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
    @PreAuthorize("hasAuthority('VIEW_REPORT')")
    public ScheduleReportResponse ScheduleReport(
            @NotBlank @PathVariable String truckId){
        return reportService.scheduleReport(truckId);
    }
}
