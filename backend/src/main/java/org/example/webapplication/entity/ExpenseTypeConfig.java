package org.example.webapplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.TypeExpense;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "expense_type_config")
public class ExpenseTypeConfig extends Base {

    @Enumerated(EnumType.STRING)
    @Column(name = "type_key", nullable = false)
    private TypeExpense key;

    private String label;

    private String description;

    private Boolean active;
}