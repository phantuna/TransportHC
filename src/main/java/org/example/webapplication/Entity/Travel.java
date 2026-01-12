package org.example.webapplication.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="travel")
@SQLDelete(sql = "UPDATE travel SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class Travel extends Base {
    @ManyToOne
    private User user;

    @ManyToOne
    private Truck truck;

    @ManyToOne
    private Schedule schedule;
    private LocalDate startDate;
    private LocalDate endDate;
    @OneToMany(mappedBy = "travel")
    private List<Expense> expenses = new ArrayList<>();
}
