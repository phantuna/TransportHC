package org.example.webapplication.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.webapplication.enums.InventoryStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryRequest {
    @NotBlank(message = "validation.item_id.not_null")
    private String itemId;

    @Positive(message = "validation.quantity.gt_zero")
    private double quantity;

    @Size(max = 300, message = "validation.description.exceed_300")
    private String description;

    @NotBlank(message = "validation.invoice_id.not_null")
    private String invoiceId;

    @NotBlank(message = "validation.customer_name.not_null")
    private String customerName;

    private InventoryStatus status;
}
