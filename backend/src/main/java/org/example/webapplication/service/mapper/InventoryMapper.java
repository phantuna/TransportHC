package org.example.webapplication.service.mapper;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.inventory.InventoryResponse;
import org.example.webapplication.entity.Inventory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryMapper {

    public InventoryResponse toResponse(Inventory inventory) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        return InventoryResponse.builder()
                .inventoryId(inventory.getId())
                .itemId(inventory.getItem().getId())
                .quantity(inventory.getQuantity())
                .description(inventory.getDescription())
                .customerName(inventory.getCustomerName())
                .invoiceId(
                        inventory.getInvoice() != null
                                ? inventory.getInvoice().getId()
                                : null
                )
                .createdDate(
                        inventory.getCreatedDate() != null
                                ? inventory.getCreatedDate()
                                : null
                )
                .status(inventory.getStatus())
                .createdBy(username)
                .modifiedBy(username)
                .build();
    }
}