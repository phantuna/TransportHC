package org.example.webapplication.dto.response.inventory;


import lombok.Builder;
import lombok.Data;
import org.example.webapplication.enums.InvoiceType;

import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {

    private String id;
    private String invoiceCode;
    private InvoiceType type;
    private String customerName;
    private LocalDateTime createdAt;

}