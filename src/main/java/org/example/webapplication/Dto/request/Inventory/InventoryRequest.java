package org.example.webapplication.Dto.request.Inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.Enum.InventoryStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryRequest {
    @NotBlank(message = "ITEM_ID_NOT_NULL")
    private String itemId;

    @Positive(message = "QUANTITY_GREATER_THAN_ZERO")
    private double quantity;

    @Size(max = 300, message = "DESCRIPTION_NOT_EXCEED_300")
    private String description;

    @NotBlank(message = "INVOICE_ID_NOT_NULL")
    private String invoiceId;

    @NotBlank(message = "CUSTOMER_NAME_NOT_NULL")
    private String customerName;

    private InventoryStatus status;
}
