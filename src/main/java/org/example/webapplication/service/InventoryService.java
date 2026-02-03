package org.example.webapplication.service;

import com.querydsl.core.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.webapplication.dto.request.inventory.InventoryRequest;
import org.example.webapplication.dto.response.inventory.InventoryResponse;
import org.example.webapplication.dto.response.inventory.InventorySummaryResponse;
import org.example.webapplication.entity.Inventory;
import org.example.webapplication.entity.Invoice;
import org.example.webapplication.entity.Item;
import org.example.webapplication.entity.QInventory;
import org.example.webapplication.enums.InventoryStatus;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.inventory.InventoryRepository;
import org.example.webapplication.repository.InvoiceRepository;
import org.example.webapplication.repository.ItemRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final InvoiceRepository invoiceRepository;
    private final PermissionService  permissionService;
    private final QInventory qInventory = QInventory.inventory;

    public InventoryResponse toResponse(Inventory inventory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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
                .createdDate(inventory.getCreatedDate())
                .status(inventory.getStatus())
                .createdBy(username)
                .modifiedBy(username)
                .build();
    }


    public InventoryResponse inventoryCreated (InventoryRequest dto){
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.INVENTORY
        );
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(()-> new AppException(ErrorCode.ITEM_NOT_FOUND));
        Invoice invoice = null;
        if (dto.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(dto.getInvoiceId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        }
        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setQuantity(dto.getQuantity());
        inventory.setDescription(dto.getDescription()); // ví dụ: "Nhập kho 9/11"
        inventory.setCustomerName(dto.getCustomerName());
        inventory.setInvoice(invoice);
        inventory.setStatus(dto.getStatus());
        Inventory saved = inventoryRepository.save(inventory);

        return toResponse(saved);
    }

    public InventoryResponse inventoryUpdated (String id,InventoryRequest dto){
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.INVENTORY
        );
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(()-> new AppException(ErrorCode.ITEM_NOT_FOUND));
        Invoice invoice = null;
        if (dto.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(dto.getInvoiceId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        inventory.setItem(item);
        inventory.setQuantity(dto.getQuantity());
        inventory.setDescription(dto.getDescription());
        inventory.setCustomerName(dto.getCustomerName());
        inventory.setInvoice(invoice);
        inventory.setModifiedBy(username);
        Inventory saved = inventoryRepository.save(inventory);

        return toResponse(saved);
    }

    public InventoryResponse getInventoryById(String id) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.INVENTORY
        );
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        return toResponse(inventory);
    }


    public Page<InventoryResponse> getAllInventories(int page, int size) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.INVENTORY
        );
        Pageable pageable = PageRequest.of(page, size);

        Page<Inventory> inventoryPage =
                inventoryRepository.findAll(pageable);

        return inventoryPage.map(this::toResponse);
    }

    @Transactional
    public void deleteInventory(String inventoryId) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.INVENTORY
        );
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        inventoryRepository.delete(inventory);
    }

    @Transactional
    public List<InventorySummaryResponse> getInventorySummary() {
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.INVENTORY
        );
        List<Tuple> results = inventoryRepository.getInventorySummary();
        List<InventorySummaryResponse> responses = new ArrayList<>();

        for (Tuple row : results) {
            String itemId = row.get(qInventory.item.id);
            Double quantity = row.get(qInventory.quantity.sum());

            responses.add(
                    InventorySummaryResponse.builder()
                            .itemId(itemId)
                            .quantity(quantity != null ? quantity : 0.0)
                            .build()
            );
        }
        return responses;
    }

    @Transactional
    public void importFromExcel(MultipartFile file) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.INVENTORY
        );
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            DataFormatter formatter = new DataFormatter();
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String itemId = row.getCell(0).getStringCellValue().trim();
                double quantity = row.getCell(1).getNumericCellValue();
                String description = row.getCell(2).getStringCellValue();
                String statusStr = formatter
                        .formatCellValue(row.getCell(3))
                        .trim()
                        .toUpperCase();

                InventoryStatus status;
                try {
                    status = InventoryStatus.valueOf(statusStr);
                } catch (IllegalArgumentException e) {
                    throw new AppException(ErrorCode.INVALID_INVENTORY_STATUS);
                }                Item item = itemRepository.findById(itemId)
                        .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
                Inventory inventory = new Inventory();
                inventory.setItem(item);
                inventory.setQuantity(quantity);
                inventory.setDescription(description);
                inventory.setStatus(status);
                inventoryRepository.save(inventory);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public ByteArrayInputStream exportToExcel()
    throws IOException{
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.INVENTORY
        );
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventory");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Item ID");
        header.createCell(1).setCellValue("Quantity");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Status");
        List<Inventory> inventories = inventoryRepository.findAll();
        int rowIdx = 1;
        for (Inventory i : inventories) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(i.getItem().getId());
            row.createCell(1).setCellValue(i.getQuantity());
            row.createCell(2).setCellValue(i.getDescription());
            row.createCell(3).setCellValue(i.getStatus().name());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Transactional
    public List<InventoryResponse> searchInventories(
            String itemId,
            InventoryStatus status,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.INVENTORY
        );
        List<Inventory> inventories = inventoryRepository.searchAndFilter(
                itemId,
                status,
                keyword,
                fromDate,
                toDate
        );

        List<InventoryResponse> responses = new ArrayList<>();
        for (Inventory inventory : inventories) {
            InventoryResponse response = toResponse(inventory);
            responses.add(response);
        }

        return responses;
    }


}
