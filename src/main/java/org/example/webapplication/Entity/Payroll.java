package org.example.webapplication.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
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

    private double baseSalary;
    private double sheduleSalary;
    private double expenseSalary;
    private double advanceSalary;
    private double totalSalary;

    @OneToOne
    @JoinColumn(
            name = "driver_id",
            referencedColumnName = "id",
            unique = true
    )
    private User user;
}
