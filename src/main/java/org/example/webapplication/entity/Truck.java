package org.example.webapplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.TruckStatus;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="truck")
@SQLDelete(sql = "UPDATE truck SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class Truck extends Base{

    private String typeTruck;
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    private String licensePlate; // bien so xe
    private boolean ganMooc;

    @Enumerated(EnumType.STRING)
    private TruckStatus status;

    @OneToMany(mappedBy = "truck")
    private List<Travel> travels = new ArrayList<>();

}
