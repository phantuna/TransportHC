package org.example.webapplication.repository.schedule;

import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.dto.response.schedule.ScheduleDocumentResponse;
import org.example.webapplication.dto.response.schedule.ScheduleResponse;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ScheduleRepositoryCustom {
    List<TravelScheduleReportResponse> getTravelScheduleReport (String truckId);
    List<ExpenseResponse> findExpensesByTravel(String truckId);
    List<ScheduleDocumentResponse> findDocumentsByTravel(String truckId);
    Page<ScheduleResponse> findSchedulePageByUsername(String username, Pageable pageable);
    List<ScheduleDocumentResponse> findDocumentsByScheduleIds(List<String> scheduleIds);



}
