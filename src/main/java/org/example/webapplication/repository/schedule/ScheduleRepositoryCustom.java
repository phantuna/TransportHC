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
    List<ScheduleResponse> findSchedulesByDriverUsername(String username);
    Page<ScheduleDocumentResponse> findDocumentsByScheduleIds(List<String> scheduleIds, Pageable pageable);



}
