package org.example.webapplication.Dto.response.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventorySummaryResponse {
    private String itemId;
    private double quantity;

}
