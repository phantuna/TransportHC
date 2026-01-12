package org.example.webapplication.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @ManyToOne
    private Item item;

    private double quantity;
    private String description;

    @ManyToOne
    private Invoice invoice;

    private String customerName;


}
