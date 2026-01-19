package org.example.webapplication.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.webapplication.Dto.request.Inventory.InventoryRequest;
import org.example.webapplication.Dto.response.Inventory.InventoryResponse;
import org.example.webapplication.Dto.response.Inventory.InventorySummaryResponse;
import org.example.webapplication.Entity.Inventory;
import org.example.webapplication.Entity.Invoice;
import org.example.webapplication.Entity.Item;
import org.example.webapplication.Enum.InventoryStatus;
import org.example.webapplication.Exception.AppException;
import org.example.webapplication.Exception.ErrorCode;
import org.example.webapplication.Repository.InventoryRepository;
import org.example.webapplication.Repository.InvoiceRepository;
import org.example.webapplication.Repository.ItemRepository;
import org.example.webapplication.Repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public InventoryResponse inventoryCreated (InventoryRequest dto){
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(()-> new AppException(ErrorCode.ITEM_NOT_FOUND));
        Invoice invoice = null;
        if (dto.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(dto.getInvoiceId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setQuantity(dto.getQuantity());
        inventory.setDescription(dto.getDescription()); // ví dụ: "Nhập kho 9/11"
        inventory.setCustomerName(dto.getCustomerName());
        inventory.setInvoice(invoice);
        inventory.setStatus(dto.getStatus());// nếu có

        Inventory saved = inventoryRepository.save(inventory);
        return InventoryResponse.builder()
                .inventoryId(saved.getId())
                .itemId(item.getId())
                .quantity(saved.getQuantity())
                .invoiceId(invoice.getId())
                .description(saved.getDescription())
                .customerName(saved.getCustomerName())
                .status(saved.getStatus())
                .build();
    }


    @PreAuthorize("hasAuthority('MANAGE_INVENTORY')")
    public InventoryResponse inventoryUpdated (String id,InventoryRequest dto){
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

        // 6. Save
        Inventory saved = inventoryRepository.save(inventory);

        // 7. Trả response
        return InventoryResponse.builder()
                .inventoryId(saved.getId())
                .itemId(saved.getItem().getId())
                .quantity(saved.getQuantity())
                .description(saved.getDescription())
                .customerName(saved.getCustomerName())
                .invoiceId(invoice.getId())
                .build();

    }

    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public InventoryResponse getInventoryById(String id) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        return InventoryResponse.builder()
                .inventoryId(inventory.getId())
                .itemId(inventory.getItem().getId())
                .quantity(inventory.getQuantity())
                .description(inventory.getDescription())
                .customerName(inventory.getCustomerName())
                .invoiceId(inventory.getInvoice().getId())
                .status(inventory.getStatus())
                .build();
    }


    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public List<InventoryResponse> getAllInventories() {

        List<Inventory> inventories = inventoryRepository.findAll();
        List<InventoryResponse> responses = new ArrayList<>();

        for (Inventory inventory : inventories) {

            InventoryResponse response = InventoryResponse.builder()
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
                    .CreatedDate(inventory.getCreatedDate())
                    .status(inventory.getStatus())
                    .build();

            responses.add(response);
        }

        return responses;
    }

    @PreAuthorize("hasAuthority('MANAGE_INVENTORY')")
    @Transactional
    public void deleteInventory(String inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        inventoryRepository.delete(inventory);
    }

    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public List<InventorySummaryResponse> getInventorySummary() {

        List<Object[]> results = inventoryRepository.getInventorySummary();
        List<InventorySummaryResponse> responses = new ArrayList<>();

        for (Object[] row : results) {
            String itemId = (String) row[0];
            Double quantity = (Double) row[1];

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
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public void importFromExcel(MultipartFile file) {
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
    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public ByteArrayInputStream exportToExcel()
    throws IOException{

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

    @PreAuthorize("hasAuthority('VIEW_INVENTORY') OR hasAuthority('MANAGE_INVENTORY')")
    public List<InventoryResponse> searchInventories(
            String itemId,
            InventoryStatus status,
            String keyword,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        List<Inventory> inventories = inventoryRepository.searchAndFilter(
                itemId,
                status,
                keyword,
                fromDate,
                toDate
        );

        List<InventoryResponse> responses = new ArrayList<>();

        for (Inventory inventory : inventories) {
            responses.add(
                    InventoryResponse.builder()
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
                            .status(inventory.getStatus())
                            .CreatedDate(inventory.getCreatedDate())
                            .build()
            );
        }

        return responses;
    }


}
