package org.example.webapplication.dto.response.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.ApprovalStatus;
import org.example.webapplication.enums.TypeExpense;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseResponse {
    private String id;
    private TypeExpense type;
    private double expense;
    private String description;
    private ApprovalStatus approval;
    private String travelId;
    private String driverName;
    private LocalDate incurredDate;
    private String modifiedBy;
    private LocalDateTime CreatedDate;

}
