package org.example.webapplication.Entity;


import jakarta.persistence.*;
import lombok.Data;
import org.example.webapplication.Enum.ApprovalStatus;
import org.example.webapplication.Enum.TypeExpense;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Data
@Table(name="expense")
@SQLDelete(sql = "UPDATE expense SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class Expense extends Base{
    @ManyToOne
    @JoinColumn(name = "travel_id")
    private Travel travel;

    @Enumerated(EnumType.STRING)
    private TypeExpense type;

    private double expense;
    private String description;
    private LocalDate incurredDate;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approval;
}
