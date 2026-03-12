package org.example.webapplication.service;

import lombok.RequiredArgsConstructor;

import org.example.webapplication.dto.request.inventory.InvoiceRequest;
import org.example.webapplication.dto.response.inventory.InvoiceResponse;
import org.example.webapplication.entity.Invoice;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.inventory.InvoiceRepository;
import org.example.webapplication.service.mapper.InvoiceMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceResponse createInvoice(InvoiceRequest request){

        Invoice invoice = invoiceMapper.toEntity(request);

        Invoice saved = invoiceRepository.save(invoice);

        return invoiceMapper.toResponse(saved);
    }

    public InvoiceResponse updateInvoice(String id, InvoiceRequest request){

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        invoice.setInvoiceCode(request.getInvoiceCode());
        invoice.setType(request.getType());
        invoice.setCustomerName(request.getCustomerName());

        Invoice saved = invoiceRepository.save(invoice);

        return invoiceMapper.toResponse(saved);
    }

    public void deleteInvoice(String id){

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        invoiceRepository.delete(invoice);
    }

    public InvoiceResponse getInvoiceById(String id){

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        return invoiceMapper.toResponse(invoice);
    }

    public List<InvoiceResponse> getAllInvoices(){

        return invoiceRepository.findAll()
                .stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

}