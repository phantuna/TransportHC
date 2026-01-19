package org.example.webapplication.Dto.response.Inventory;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.Enum.InventoryStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponse {
    private String inventoryId;
    private String itemId;

    private double quantity;
    private String description;
    private String invoiceId;

    private String customerName;
    private LocalDateTime CreatedDate;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;
}
