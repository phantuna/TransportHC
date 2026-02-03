package org.example.webapplication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.inventory.InventoryRequest;
import org.example.webapplication.dto.response.inventory.InventoryResponse;
import org.example.webapplication.dto.response.inventory.InventorySummaryResponse;
import org.example.webapplication.enums.InventoryStatus;
import org.example.webapplication.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/inventory")
@RestController
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/created")
    @PreAuthorize("isAuthenticated()")
    public InventoryResponse created (@Valid  @RequestBody InventoryRequest dto)
    {
        return inventoryService.inventoryCreated(dto);
    }

    @PutMapping("/updated/{id}")
    @PreAuthorize("isAuthenticated()")
    public InventoryResponse updated (@Valid @PathVariable String id , @RequestBody InventoryRequest dto){
        return inventoryService.inventoryUpdated(id, dto);
    }

    @GetMapping("/getbyId/{id}")
    @PreAuthorize("isAuthenticated()")
    public InventoryResponse getById(@Valid @PathVariable String id){
        return inventoryService.getInventoryById(id);
    }

    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public Page<InventoryResponse> getAll(int page , int size){
        return inventoryService.getAllInventories(page ,size);
    }

    @DeleteMapping("/deleted/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleted (@Valid @PathVariable String id){
        inventoryService.deleteInventory(id);
    }

    @PostMapping("/import")
    @PreAuthorize("isAuthenticated()")
    public void importInventory(@Valid @RequestParam MultipartFile file)
    throws IOException {
        inventoryService.importFromExcel(file);
    }

    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportInventory() throws IOException {
        ByteArrayInputStream in = inventoryService.exportToExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory_import.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(in.readAllBytes());
    }
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public List<InventorySummaryResponse> getInventorySummary() {
        return inventoryService.getInventorySummary();
    }


    @GetMapping("/search")
    public List<InventoryResponse> search(
            @RequestParam(required = false) String itemId,
            @RequestParam(required = false) InventoryStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate toDate
    ) {
        return inventoryService.searchInventories(
                itemId, status, keyword, fromDate, toDate
        );
    }


}
