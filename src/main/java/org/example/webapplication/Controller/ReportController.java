package org.example.webapplication.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Dto.request.PayrollRequest;
import org.example.webapplication.Dto.request.TruckExpenseRequest;
import org.example.webapplication.Dto.response.ExpenseReportResponse;
import org.example.webapplication.Dto.response.PayrollDetailResponse;
import org.example.webapplication.Dto.response.PayrollResponse;
import org.example.webapplication.Dto.response.ScheduleReportResponse;
import org.example.webapplication.Service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/payroll/my")
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
    public List<PayrollDetailResponse> payrollAllByMonth(
            @RequestParam int month,
            @RequestParam int year
    ) {
        return reportService.payrollAllByMonth(month, year);
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
