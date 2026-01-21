package org.example.webapplication.dto.response.travel;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.dto.response.schedule.ScheduleDocumentResponse;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TravelScheduleResponse {
    private String travelId;
    private String scheduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ExpenseResponse> expense;
    private List<ScheduleDocumentResponse> document;

    private double totalExpense;
}
