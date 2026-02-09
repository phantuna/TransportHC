package org.example.webapplication.service;

import com.querydsl.core.Tuple;
import org.example.webapplication.dto.response.expense.DriverExpenseItemResponse;
import org.example.webapplication.dto.response.expense.DriverExpenseReportResponse;
import org.example.webapplication.dto.response.report.ExpenseReportDetailResponse;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.dto.response.report.ExpenseSummaryResponse;
import org.example.webapplication.dto.response.schedule.ScheduleDocumentResponse;
import org.example.webapplication.dto.response.schedule.ScheduleReportResponse;
import org.example.webapplication.dto.response.travel.TravelDailyReportItemResponse;
import org.example.webapplication.dto.response.travel.TravelDailyReportResponse;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.enums.TypeExpense;
import org.example.webapplication.repository.expense.ExpenseRepository;
import org.example.webapplication.repository.schedule.ScheduleRepository;
import org.example.webapplication.repository.travel.TravelRepository;
import org.example.webapplication.repository.truck.TruckRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.travel.TravelScheduleResponse;
import org.example.webapplication.entity.*;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final TruckRepository truckRepository;
    private final PermissionService permissionService;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final TravelRepository travelRepository;




    @Cacheable(value= "report_truck_summary", key = "{#page, #size}")
    public Page<ExpenseSummaryResponse> allTruckExpenseSummaryReport(int page, int size
    ) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.REPORT
        );
        Pageable pageable = PageRequest.of(page, size);
        return truckRepository.getAllTruckExpenseSummary(pageable);
    }


    @Cacheable(value= "report_truck_detail", key = "{#truckId, #from, #to, #page, #size}")
    public Page<ExpenseReportDetailResponse> truckExpenseDetail(String truckId, LocalDate from, LocalDate to, int page, int size
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

    @Cacheable(value= "report_schedule", key = "#truckId")
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

    @Cacheable(value= "report_driver_expense", key = "{#driverId, #month, #year}")
    @Transactional(readOnly = true)
    public DriverExpenseReportResponse driverExpenseReport(
            String driverId,
            int month,
            int year
    ) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.REPORT
        );

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        List<Tuple> rawData =
                expenseRepository.sumExpenseByDriverAndMonth(driverId, from, to);

        List<DriverExpenseItemResponse> items = new ArrayList<>();
        double total = 0;
        int order = 1;

        for (Tuple row : rawData) {
            TypeExpense type = row.get(0, TypeExpense.class);
            Double amount = row.get(1, Double.class);

            total += amount;

            items.add(new DriverExpenseItemResponse(
                    order++,
                    type,
                    amount
            ));
        }

        return DriverExpenseReportResponse.builder()
                .driverId(driver.getId())
                .driverName(driver.getUsername())
                .month(month)
                .year(year)
                .totalExpense(total)
                .items(items)
                .build();
        }

    @Cacheable(value= "report_travel_daily", key = "{#truckId, #month, #year}")
        @Transactional(readOnly = true)
        public TravelDailyReportResponse travelDailyReport(String truckId,int month, int year ) {
            permissionService.getUser(
                    List.of(PermissionKey.VIEW),
                    PermissionType.REPORT
            );

            // 1. Lấy raw data từ repository
            List<TravelDailyReportItemResponse> items =
                    travelRepository.dailyTravelReport(truckId, month, year);

            // 2. Set STT + tính tổng số chuyến
            long totalTrip = 0;
            int stt = 1;

            for (TravelDailyReportItemResponse item : items) {
                item.setStt(stt++);
                totalTrip += Optional.ofNullable(item.getTripCount()).orElse(0L);
            }

            // 3. Trả response
            return TravelDailyReportResponse.builder()
                    .truckId(truckId)
                    .month(month)
                    .year(year)
                    .totalTrip(totalTrip)
                    .items(items)
                    .build();
        }
    }



