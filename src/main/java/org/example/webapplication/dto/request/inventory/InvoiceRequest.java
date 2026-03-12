package org.example.webapplication.dto.request.inventory;

import lombok.Data;
import org.example.webapplication.enums.InvoiceType;

@Data
public class InvoiceRequest {

    private String invoiceCode;
    private InvoiceType type;
    private String customerName;

}