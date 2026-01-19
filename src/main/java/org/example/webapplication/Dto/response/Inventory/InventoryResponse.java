package org.example.webapplication.Dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
}
