package org.example.webapplication.service.mapper;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.entity.Expense;
import org.example.webapplication.entity.Truck;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {
    public ExpenseResponse toResponse(Expense expense) {

        Truck truck = expense.getTravel().getTruck();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .type(expense.getType())
                .expense(expense.getExpense())
                .description(expense.getDescription())
                .approval(expense.getApproval())
                .travelId(expense.getTravel().getId())
                .driverName(
                        truck != null && truck.getDriver() != null
                                ? truck.getDriver().getUsername()
                                : null
                )
                .incurredDate(expense.getIncurredDate())
                .modifiedBy(expense.getModifiedBy())
                .createdDate(expense.getCreatedDate())
                .build();
    }
}
