package org.example.webapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.webapplication.BaseMySQLIntegrationTest;
import org.example.webapplication.dto.request.inventory.InventoryRequest;
import org.example.webapplication.entity.*;
import org.example.webapplication.entity.Role_Permission.Permission;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.enums.InventoryStatus;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.enums.InvoiceType;
import org.example.webapplication.repository.*;
import org.example.webapplication.repository.inventory.InventoryRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class InventoryControllerIntegration extends BaseMySQLIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    private Item item;
    private Invoice invoice;
    private InventoryRequest validRequest;

    @BeforeEach
    void setUp() {

        seedPermission(PermissionKey.VIEW, PermissionType.INVENTORY);
        seedPermission(PermissionKey.MANAGE, PermissionType.INVENTORY);

        seedRole("R_ADMIN", "ADMIN");

        Role adminRole = roleRepository.findById("R_ADMIN").orElseThrow();
        adminRole.setPermissions(permissionRepository.findAll());
        roleRepository.save(adminRole);

        userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername("admin");
                    u.setPassword("123456");
                    u.setRoles(List.of(adminRole));
                    return userRepository.save(u);
                });

        item = new Item();
        item.setCode("ITEM001");
        item.setName("Oil");
        item.setUnit("Litre");
        item = itemRepository.save(item);

        invoice = new Invoice();
        invoice.setInvoiceCode("INV001");
        invoice.setType(InvoiceType.IMPORT);
        invoice.setCustomerName("ABC Company");
        invoice.setCreatedAt(LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);

        validRequest = InventoryRequest.builder()
                .itemId(item.getId())
                .quantity(100)
                .description("Import inventory")
                .invoiceId(invoice.getId())
                .customerName("ABC Company")
                .status(InventoryStatus.IMPORT)
                .build();
    }

    /* ================= CREATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void createInventory_success() throws Exception {

        mockMvc.perform(
                        post("/inventory/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.status").value("IMPORT"));
    }

    /* ================= UPDATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void updateInventory_success() throws Exception {

        String response = mockMvc.perform(
                        post("/inventory/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String inventoryId = objectMapper.readTree(response).get("inventoryId").asText();

        validRequest.setQuantity(200);

        mockMvc.perform(
                        put("/inventory/updated/{id}", inventoryId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(200));
    }

    /* ================= GET ALL ================= */

    @Test
    @WithMockUser(username = "admin")
    void getAllInventory_success() throws Exception {

        mockMvc.perform(
                post("/inventory/created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
        );

        mockMvc.perform(
                        get("/inventory/getAll")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    /* ================= DELETE ================= */

    @Test
    @WithMockUser(username = "admin")
    void deleteInventory_success() throws Exception {

        String response = mockMvc.perform(
                        post("/inventory/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String inventoryId = objectMapper.readTree(response).get("inventoryId").asText();

        mockMvc.perform(
                        delete("/inventory/deleted/{id}", inventoryId)
                )
                .andExpect(status().isOk());
    }

    /* ================= EXPORT ================= */

    @Test
    @WithMockUser(username = "admin")
    void exportInventory_success() throws Exception {

        mockMvc.perform(
                        get("/inventory/export")
                )
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("inventory_import.xlsx")));
    }

    /* ================= IMPORT ================= */

    @Test
    @WithMockUser(username = "admin")
    void importInventory_success() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "inventory.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(
                        multipart("/inventory/import")
                                .file(file)
                )
                .andExpect(status().isOk());
    }
}
