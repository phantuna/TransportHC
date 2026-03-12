package org.example.webapplication.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE item SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class Item extends Base{
    private String code;
    private String name;
    private String unit;
}
