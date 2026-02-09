package org.example.webapplication.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE payroll SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class Payroll extends Base{
    private int month;
    private int year;

    private double baseSalary;
    private double sheduleSalary;
    private double expenseSalary;
    private double advanceSalary;
    private double totalSalary;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User user;
}
