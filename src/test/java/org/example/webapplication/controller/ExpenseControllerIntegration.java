package org.example.webapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.webapplication.BaseMySQLIntegrationTest;
import org.example.webapplication.dto.request.expense.ExpenseRequest;
import org.example.webapplication.entity.*;
import org.example.webapplication.entity.Role_Permission.Permission;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.enums.*;
import org.example.webapplication.repository.PermissionRepository;
import org.example.webapplication.repository.RoleRepository;
import org.example.webapplication.repository.expense.ExpenseRepository;
import org.example.webapplication.repository.schedule.ScheduleRepository;
import org.example.webapplication.repository.travel.TravelRepository;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ExpenseControllerIntegration extends BaseMySQLIntegrationTest {

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
    TruckRepository truckRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    TravelRepository travelRepository;

    @Autowired
    ExpenseRepository expenseRepository;

    private User admin;
    private Truck truck;
    private Schedule schedule;
    private Travel travel;
    private ExpenseRequest validRequest;


    @BeforeEach
    void setUp() {

        seedPermission(PermissionKey.CREATE, PermissionType.EXPENSE);
        seedPermission(PermissionKey.UPDATE, PermissionType.EXPENSE);
        seedPermission(PermissionKey.VIEW, PermissionType.EXPENSE);
        seedPermission(PermissionKey.MANAGE, PermissionType.EXPENSE);
        seedPermission(PermissionKey.APPROVE, PermissionType.EXPENSE);

        seedRole("R_ADMIN", "ADMIN");

        Role adminRole = roleRepository.findById("R_ADMIN").orElseThrow();
        adminRole.setPermissions(permissionRepository.findAll());
        roleRepository.save(adminRole);

        admin = userRepository.findByUsername("admin")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername("admin");
                    u.setPassword("123456");
                    u.setRoles(List.of(adminRole));
                    return userRepository.save(u);
                });

        truck = new Truck();
        truck.setLicensePlate("30A-11111");
        truck.setTypeTruck("Container");
        truck.setGanMooc(true);
        truck.setStatus(TruckStatus.AVAILABLE);
        truck.setDriver(admin);
        truck = truckRepository.save(truck);

        schedule = new Schedule();
        schedule.setStartPlace("Ha Noi");
        schedule.setEndPlace("Hai Phong");
        schedule.setExpense(500000);
        schedule = scheduleRepository.save(schedule);

        travel = new Travel();
        travel.setTruck(truck);
        travel.setSchedule(schedule);
        travel.setUser(admin);
        travel.setStartDate(LocalDate.now());
        travel.setEndDate(LocalDate.now().plusDays(1));
        travel = travelRepository.save(travel);

        validRequest = ExpenseRequest.builder()
                .type(TypeExpense.FUEL)
                .expense(200000)
                .description("Fuel cost")
                .travelId(travel.getId())
                .incurredDate(LocalDate.now())
                .build();
    }

    /* ================= CREATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void createExpense_success() throws Exception {

        mockMvc.perform(
                        post("/expense/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FUEL"))
                .andExpect(jsonPath("$.approval").value("PENDING_APPROVAL"));
    }

    /* ================= UPDATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void updateExpense_success() throws Exception {

        String response = mockMvc.perform(
                        post("/expense/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expenseId = objectMapper.readTree(response).get("id").asText();

        ExpenseRequest update = ExpenseRequest.builder()
                .type(TypeExpense.REPAIR)
                .expense(300000)
                .description("Repair cost")
                .travelId(travel.getId())
                .incurredDate(LocalDate.now())
                .build();

        mockMvc.perform(
                        put("/expense/updated/{id}", expenseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("REPAIR"));
    }

    /* ================= APPROVAL ================= */

    @Test
    @WithMockUser(username = "admin")
    void approveExpense_success() throws Exception {

        String response = mockMvc.perform(
                        post("/expense/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expenseId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(
                        post("/expense/approval")
                                .param("id", expenseId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approval").value("APPROVED"));
    }

    /* ================= GET ALL ================= */

    @Test
    @WithMockUser(username = "admin")
    void getAllExpenses_success() throws Exception {

        mockMvc.perform(
                post("/expense/created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
        );

        mockMvc.perform(
                        get("/expense/getAll")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    /* ================= DELETE ================= */

    @Test
    @WithMockUser(username = "admin")
    void deleteExpense_success() throws Exception {

        String response = mockMvc.perform(
                        post("/expense/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expenseId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(
                        delete("/expense/delete/{id}", expenseId)
                )
                .andExpect(status().isOk());
    }
}
