package org.example.webapplication.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.Enum.InvoiceType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="invoice")
@SQLDelete(sql = "UPDATE invoice SET deleted = 1, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = 0")
public class Invoice extends Base{
    private String invoiceCode;

    @Enumerated(EnumType.STRING)
    private InvoiceType type; // IMPORT / EXPORT

    private String customerName;

    private LocalDateTime createdAt;

    @ManyToOne
    private User createdBy;
}
