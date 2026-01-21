package org.example.webapplication.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.webapplication.enums.ApprovalStatus;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Data
@Table(name="schedule")
@SQLDelete(sql = "UPDATE schedule SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class Schedule extends Base{
    private String startPlace;
    private String endPlace;
    private double expense;
    private String description;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approval;

    @ManyToMany
    @JoinTable(
            name = "schedule_driver",
            joinColumns = @JoinColumn(name = "schedule_id"),
            inverseJoinColumns = @JoinColumn(name = "driver_id")
    )
    private List<User> drivers = new ArrayList<>();;

    @OneToMany(mappedBy = "schedule", cascade = ALL, orphanRemoval = true)
    private List<ScheduleDocument> documents = new ArrayList<>();
}
