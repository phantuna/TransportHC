package org.example.webapplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.InventoryStatus;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="inventory")
@SQLDelete(sql = "UPDATE inventory SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class Inventory extends Base{
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    private double quantity;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Invoice invoice;

    private String customerName;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

}
