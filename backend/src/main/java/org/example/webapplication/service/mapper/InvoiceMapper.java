package org.example.webapplication.service.mapper;

import org.example.webapplication.dto.request.inventory.InvoiceRequest;
import org.example.webapplication.dto.response.inventory.InvoiceResponse;
import org.example.webapplication.entity.Invoice;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;

@Component
public class InvoiceMapper {

    public Invoice toEntity(InvoiceRequest request){

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(request.getInvoiceCode());
        invoice.setType(request.getType());
        invoice.setCustomerName(request.getCustomerName());
        invoice.setCreatedAt(LocalDateTime.now());

        return invoice;
    }

    public InvoiceResponse toResponse(Invoice invoice){

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceCode(invoice.getInvoiceCode())
                .type(invoice.getType())
                .customerName(invoice.getCustomerName())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

}