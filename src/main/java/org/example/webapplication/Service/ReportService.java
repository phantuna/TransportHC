package org.example.webapplication.Service;

import org.example.webapplication.Dto.request.Truck.TruckExpenseRequest;
import org.example.webapplication.Dto.response.Expense.ExpenseReportResponse;
import org.example.webapplication.Dto.response.Expense.ExpenseResponse;
import org.example.webapplication.Dto.response.Payroll.PayrollDetailResponse;
import org.example.webapplication.Dto.response.Schedule.ScheduleDocumentResponse;
import org.example.webapplication.Dto.response.Schedule.ScheduleReportResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Enum.ApprovalStatus;
import org.example.webapplication.Dto.response.Travel.TravelScheduleResponse;
import org.example.webapplication.Entity.*;
import org.example.webapplication.Exception.AppException;
import org.example.webapplication.Exception.ErrorCode;
import org.example.webapplication.Repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final PayrollRepository payrollRepository;
    private final TruckRepository truckRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;


    private PayrollDetailResponse buildPayrollDetail(
            User driver,
            int month,
            int year
    ) {
        double baseSalary = driver.getBaseSalary();

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        List<Travel> travels =
                travelRepository.findByUserAndStartDateBetween(driver, from, to);

        double expenseSalary = 0;

        for (Travel travel : travels) {
            if (travel.getExpenses() == null) continue;
            for (Expense e : travel.getExpenses()) {
                if (e.getApproval() == ApprovalStatus.APPROVED) {
                    expenseSalary += e.getExpense();
                }
            }
        }

        double advance = payrollRepository.findByUser(driver)
                .map(Payroll::getAdvanceSalary)
                .orElse(0.0);

        double total = baseSalary + expenseSalary - advance;

        return PayrollDetailResponse.builder()
                .driverId(driver.getId())
                .name(driver.getUsername())
                .month(month)
                .year(year)
                .baseSalary(baseSalary)
                .expenseSalary(expenseSalary)
                .advanceSalary(advance)
                .totalSalary(total)
                .build();
    }

    @PreAuthorize("hasAuthority('VIEW_REPORT')")
    public PayrollDetailResponse myPayrollByMonth(int month, int year) {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));

        return buildPayrollDetail(driver, month, year);
    }

    @PreAuthorize("hasAuthority('MANAGE_REPORT')")
    @Transactional
    public List<PayrollDetailResponse> payrollAllByMonth(int month, int year) {

        List<User> drivers = userRepository.findAll();
        List<PayrollDetailResponse> result = new ArrayList<>();

        for (User d : drivers) {
            result.add(buildPayrollDetail(d, month, year));
        }

        return result;
    }



    @PreAuthorize("hasAuthority('VIEW_REPORT')")
    public ExpenseReportResponse TruckExpenseReport (TruckExpenseRequest request){
        Truck truck =truckRepository.findById(request.getTruckId())
                .orElseThrow(() -> new AppException(ErrorCode.TRUCK_NOT_FOUND));

        List<Travel> travels = travelRepository.findByTruck_IdAndStartDateBetween(request.getTruckId(), request.getFromDate(),request.getToDate());
        double total = 0;
        List<ExpenseResponse> responses = new ArrayList<>();
        String driverName = truck.getDriver().getUsername();
        for(Travel travel : travels){
            for(Expense expense : travel.getExpenses()){
                if (expense.getApproval() == ApprovalStatus.APPROVED){
                    total += expense.getExpense();
                }
                ExpenseResponse expenseResponse = new ExpenseResponse();
                expenseResponse.setId(expense.getId());
                expenseResponse.setExpense(expense.getExpense());
                expenseResponse.setType(expense.getType());
                expenseResponse.setDescription(expense.getDescription());
                expenseResponse.setApproval(expense.getApproval());
                expenseResponse.setTravelId(travel.getId());
                expenseResponse.setDriverName(driverName);
                expenseResponse.setIncurredDate(expense.getIncurredDate());
                responses.add(expenseResponse);
            }
        }

        return ExpenseReportResponse.builder()
                .truckId(truck.getId())
                .licensePlate(truck.getLicensePlate())
                .expenses(responses)
                .totalExpense(total)
                .driverName(driverName)
                .incurredDate(LocalDate.now())
                .build();
    }

    @PreAuthorize("hasAuthority('VIEW_REPORT')")
    public List<ExpenseReportResponse> allTruckExpenseReport() {

        List<Truck> trucks = truckRepository.findAll();
        List<ExpenseReportResponse> reports = new ArrayList<>();

        for (Truck truck : trucks) {
            double total = 0;
            List<ExpenseResponse> expenses = new ArrayList<>();

            String driverName = null;
            if (truck.getDriver() != null) {
                driverName = truck.getDriver().getUsername();
            }
            for (Travel travel : truck.getTravels()) {
                if (travel.getExpenses() != null) {
                    for (Expense expense : travel.getExpenses()) {

                        if (expense.getApproval() == ApprovalStatus.APPROVED) {
                            total += expense.getExpense();
                        }


                        ExpenseResponse er = ExpenseResponse.builder()
                                .id(expense.getId())
                                .type(expense.getType())
                                .expense(expense.getExpense())
                                .description(expense.getDescription())
                                .approval(expense.getApproval())
                                .travelId(travel.getId())
                                .driverName(driverName)
                                .build();

                        expenses.add(er);
                    }
                }
            }

            ExpenseReportResponse report = ExpenseReportResponse.builder()
                    .truckId(truck.getId())
                    .licensePlate(truck.getLicensePlate())
                    .expenses(expenses)
                    .totalExpense(total)
                    .driverName(driverName)
                    .build();

            reports.add(report);
        }

        return reports;
    }

    @PreAuthorize("hasAuthority('VIEW_REPORT')")
    public ScheduleReportResponse scheduleReport(String truckId) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new AppException(ErrorCode.TRUCK_NOT_FOUND));


        List<Travel> travels = travelRepository.findByTruck_Id(truckId);
        List<TravelScheduleResponse> travelResponses = new ArrayList<>();
        double  grandTotal = 0;

        for (Travel travel : travels) {

            double travelTotal = 0;
            if (travel.getSchedule() != null) {
                travelTotal += travel.getSchedule().getExpense();
            }

            List<ExpenseResponse> expenseResponses = new ArrayList<>();

            if (travel.getExpenses() != null) {
                for (Expense expense : travel.getExpenses()) {
                    if (expense.getApproval() == ApprovalStatus.APPROVED) {
                        travelTotal += expense.getExpense();
                    }
                    String driverName = null;
                    if (truck.getDriver() != null) {
                        driverName = truck.getDriver().getUsername();
                    }
                    expenseResponses.add(
                            ExpenseResponse.builder()
                                    .id(expense.getId())
                                    .type(expense.getType())
                                    .expense(expense.getExpense())
                                    .description(expense.getDescription())
                                    .approval(expense.getApproval())
                                    .travelId(travel.getId())
                                    .driverName(driverName)
                                    .build()
                    );
                }
            }

            List<ScheduleDocumentResponse> documentResponses = new ArrayList<>();
            if (travel.getSchedule() != null && travel.getSchedule().getDocuments() != null) {
                for (ScheduleDocument doc : travel.getSchedule().getDocuments()) {
                    documentResponses.add(
                            ScheduleDocumentResponse.builder()
                                    .fileName(doc.getFileName())
                                    .fileUrl(doc.getFileUrl())
                                    .fileType(doc.getFileType())
                                    .fileSize(doc.getFileSize())
                                    .build()
                    );
                }
            }

            grandTotal += travelTotal;

            travelResponses.add(
                    TravelScheduleResponse.builder()
                            .travelId(travel.getId())
                            .scheduleName(
                                    travel.getSchedule().getStartPlace() + " - " +
                                            travel.getSchedule().getEndPlace()
                            )
                            .startDate(travel.getStartDate())
                            .endDate(travel.getEndDate())
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
