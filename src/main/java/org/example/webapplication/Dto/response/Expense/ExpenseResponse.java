package org.example.webapplication.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.Enum.ApprovalStatus;
import org.example.webapplication.Enum.TypeExpense;

import java.time.LocalDate;

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

}
