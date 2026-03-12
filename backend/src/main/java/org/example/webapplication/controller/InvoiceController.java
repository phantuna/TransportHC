package org.example.webapplication.controller;

import lombok.RequiredArgsConstructor;

import org.example.webapplication.dto.request.inventory.InvoiceRequest;
import org.example.webapplication.dto.response.inventory.InvoiceResponse;
import org.example.webapplication.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public InvoiceResponse create(@RequestBody InvoiceRequest request){
        return invoiceService.createInvoice(request);
    }

    @PutMapping("/{id}")
    public InvoiceResponse update(@PathVariable String id,
                                  @RequestBody InvoiceRequest request){
        return invoiceService.updateInvoice(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id){
        invoiceService.deleteInvoice(id);
    }

    @GetMapping("/{id}")
    public InvoiceResponse getById(@PathVariable String id){
        return invoiceService.getInvoiceById(id);
    }

    @GetMapping
    public List<InvoiceResponse> getAll(){
        return invoiceService.getAllInvoices();
    }

}