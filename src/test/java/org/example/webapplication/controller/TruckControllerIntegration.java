package org.example.webapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.webapplication.BaseMySQLIntegrationTest;
import org.example.webapplication.dto.request.truck.TruckRequest;
import org.example.webapplication.entity.Role_Permission.Permission;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.enums.TruckStatus;
import org.example.webapplication.repository.PermissionRepository;
import org.example.webapplication.repository.RoleRepository;
import org.example.webapplication.repository.truck.TruckRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TruckControllerIntegration extends BaseMySQLIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TruckRepository truckRepository;

    private TruckRequest validRequest;



    /* ================= INIT ================= */

    @BeforeEach
    void setUp() {
        seedPermission(PermissionKey.CREATE, PermissionType.TRUCK);
        seedPermission(PermissionKey.MANAGE, PermissionType.TRUCK);
        seedPermission(PermissionKey.VIEW, PermissionType.TRUCK);
        seedPermission(PermissionKey.UPDATE, PermissionType.TRUCK);


        seedRole("R_ADMIN", "ADMIN");

        Role admin = roleRepository.findById("R_ADMIN").orElseThrow();
        admin.setPermissions(permissionRepository.findAll());
        roleRepository.save(admin);

        validRequest = TruckRequest.builder()
                .typeTruck("Container")
                .licensePlate("30A-12345")
                .ganMooc(true)
                .status(TruckStatus.AVAILABLE)
                .build();
    }

    /* ================= TEST ================= */

    @Test
    @WithMockUser(username = "admin")
    void createTruck_success() throws Exception {

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/truck/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("30A-12345"))
                .andExpect(jsonPath("$.typeTruck").value("Container"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @WithMockUser(username = "admin")
    void createTruck_duplicateLicensePlate_fail() throws Exception {

        mockMvc.perform(
                MockMvcRequestBuilders.post("/truck/created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
        ).andExpect(status().isOk());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/truck/created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
        ).andExpect(status().isBadRequest());
    }

    /* ================= TEST GET ALL ================= */

    @Test
    @WithMockUser(username = "admin")
    void getAllTrucks_success() throws Exception {

        mockMvc.perform(
                MockMvcRequestBuilders.post("/truck/created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/truck/getAll")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].licensePlate").exists());
    }

    /* ================= TEST UPDATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void updateTruck_success() throws Exception {

        String response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/truck/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String truckId = objectMapper.readTree(response).get("id").asText();

        TruckRequest update = TruckRequest.builder()
                .typeTruck("Tanker")
                .licensePlate("30B-88888")
                .ganMooc(false)
                .status(TruckStatus.IN_USE)
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/truck/updated/{truckId}", truckId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeTruck").value("Tanker"))
                .andExpect(jsonPath("$.status").value("IN_USE"));
    }

    /* ================= TEST DELETE (SOFT) ================= */

    @Test
    @WithMockUser(username = "admin")
    void deleteTruck_softDelete_success() throws Exception {

        String response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/truck/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String truckId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/truck/delete/{id}", truckId)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/truck/getAll")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

}
