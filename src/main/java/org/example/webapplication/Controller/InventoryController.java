package org.example.webapplication.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Dto.request.InventoryRequest;
import org.example.webapplication.Dto.response.InventoryResponse;
import org.example.webapplication.Dto.response.InventorySummaryResponse;
import org.example.webapplication.Service.InventoryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RequestMapping("/inventory")
@RestController
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/created")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public InventoryResponse created (@Valid  @RequestBody InventoryRequest dto)
    {
        return inventoryService.inventoryCreated(dto);
    }

    @PutMapping("/updated/{id}")
    @PreAuthorize("hasAuthority('MANAGE_INVENTORY')")
    public InventoryResponse updated (@Valid @PathVariable String id , @RequestBody InventoryRequest dto){
        return inventoryService.inventoryUpdated(id, dto);
    }

    @GetMapping("/getbyId/{id}")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public InventoryResponse getById(@Valid @PathVariable String id){
        return inventoryService.getInventoryById(id);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public List<InventoryResponse> getAll(){
        return inventoryService.getAllInventories();
    }

    @DeleteMapping("/deleted/{id}")
    @PreAuthorize("hasAuthority('MANAGE_INVENTORY')")
    public void deleted (@Valid @PathVariable String id){
        inventoryService.deleteInventory(id);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public void importInventory(@Valid @RequestParam MultipartFile file)
    throws IOException {
        inventoryService.importFromExcel(file);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public ResponseEntity<byte[]> exportInventory() throws IOException {
        ByteArrayInputStream in = inventoryService.exportToExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory_import.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(in.readAllBytes());
    }
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public List<InventorySummaryResponse> getInventorySummary() {
        return inventoryService.getInventorySummary();
    }


}
