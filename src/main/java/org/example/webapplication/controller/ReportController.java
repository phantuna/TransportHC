package org.example.webapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.truck.TruckExpenseRequest;
import org.example.webapplication.dto.response.expense.ExpenseReportResponse;
import org.example.webapplication.dto.response.payroll.PayrollDetailResponse;
import org.example.webapplication.dto.response.schedule.ScheduleReportResponse;
import org.example.webapplication.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/truckExpense")
    @PreAuthorize("hasAuthority('VIEW_REPORT')")
    public ExpenseReportResponse TruckExpenseReport(@Valid @RequestBody TruckExpenseRequest request){
        return reportService.TruckExpenseReport(request);
    }

    @GetMapping("/allTruckExpense")
    @PreAuthorize("hasAuthority('VIEW_REPORT')")
    public List<ExpenseReportResponse> allTruckExpenseReport(){
        return reportService.allTruckExpenseReport();
    }

    @GetMapping("/scheduleReport/{truckId}")
    @PreAuthorize("hasAuthority('VIEW_REPORT')")
    public ScheduleReportResponse ScheduleReport(@NotBlank @PathVariable String truckId){
        return reportService.scheduleReport(truckId);
    }
}
