package org.example.webapplication.service.mapper;

import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.dto.response.travel.TravelResponse;
import org.example.webapplication.entity.Expense;
import org.example.webapplication.entity.Schedule;
import org.example.webapplication.entity.Travel;
import org.example.webapplication.entity.Truck;
import org.example.webapplication.enums.ApprovalStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class TravelMapper {

    public TravelResponse toResponse(Travel travel) {

        Truck truck = travel.getTruck();
        Schedule schedule = travel.getSchedule();

        double totalApprovedExpense = 0;
        List<ExpenseResponse> expenses = List.of();

        if (travel.getExpenses() != null) {
            expenses = new ArrayList<>();
            for (Expense e : travel.getExpenses()) {
                if (e.getApproval() == ApprovalStatus.APPROVED) {
                    totalApprovedExpense += e.getExpense();
                }
                expenses.add(
                        ExpenseResponse.builder()
                                .id(e.getId())
                                .type(e.getType())
                                .expense(e.getExpense())
                                .approval(e.getApproval())
                                .travelId(travel.getId())
                                .incurredDate(e.getIncurredDate())
                                .driverName(
                                        travel.getUser() != null
                                                ? travel.getUser().getUsername()
                                                : null
                                )
                                .build()
                );
            }
        }

        return TravelResponse.builder()
                .travelId(travel.getId())
                .truckPlate(truck != null ? truck.getLicensePlate() : null)
                .driverName(travel.getUser() != null ? travel.getUser().getUsername() : null)
                .scheduleName(
                        schedule != null
                                ? schedule.getStartPlace() + " - " + schedule.getEndPlace()
                                : null
                )
                .startDate(travel.getStartDate())
                .endDate(travel.getEndDate())
                .expenses(expenses)
                .totalExpense(totalApprovedExpense)
                .build();
    }
}
