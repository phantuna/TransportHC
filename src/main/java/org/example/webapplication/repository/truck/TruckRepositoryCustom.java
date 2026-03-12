package org.example.webapplication.repository.truck;

import org.example.webapplication.dto.response.report.ExpenseReportDetailResponse;
import org.example.webapplication.dto.response.report.ExpenseSummaryResponse;
import org.example.webapplication.entity.Truck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TruckRepositoryCustom {

    Page<ExpenseSummaryResponse> getAllTruckExpenseSummary(Pageable pageable);
    Page<ExpenseReportDetailResponse> getExpenseDetailsByTruck(String truckId, LocalDate from, LocalDate to,Pageable pageable);

}
