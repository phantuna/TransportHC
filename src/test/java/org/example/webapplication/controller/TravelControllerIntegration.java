package org.example.webapplication.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.webapplication.BaseMySQLIntegrationTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;



import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.webapplication.BaseMySQLIntegrationTest;
import org.example.webapplication.dto.request.travel.TravelRequest;
import org.example.webapplication.entity.Schedule;
import org.example.webapplication.entity.Truck;
import org.example.webapplication.entity.User;
import org.example.webapplication.entity.Role_Permission.Permission;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.enums.TruckStatus;
import org.example.webapplication.repository.PermissionRepository;
import org.example.webapplication.repository.RoleRepository;
import org.example.webapplication.repository.schedule.ScheduleRepository;
import org.example.webapplication.repository.truck.TruckRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.example.webapplication.service.QuartzService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class TravelControllerIntegration extends BaseMySQLIntegrationTest {

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

    @MockBean
    QuartzService quartzTravelService;

    private Truck truck;
    private Schedule schedule;
    private User admin;
    private TravelRequest validRequest;

    @BeforeEach
    void setUp() {

        seedPermission(PermissionKey.MANAGE, PermissionType.TRAVEL);
        seedPermission(PermissionKey.VIEW, PermissionType.TRAVEL);

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
        truck.setLicensePlate("30A-99999");
        truck.setTypeTruck("Container");
        truck.setGanMooc(true);
        truck.setStatus(TruckStatus.AVAILABLE);
        truck.setDriver(admin);
        truck = truckRepository.save(truck);

        schedule = new Schedule();
        schedule.setStartPlace("Ha Noi");
        schedule.setEndPlace("Hai Phong");
        schedule.setExpense(500000);
        scheduleRepository.save(schedule);

        validRequest = new TravelRequest(
                truck.getId(),
                schedule.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );
    }

    /* ================= CREATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void createTravel_success() throws Exception {

        mockMvc.perform(
                        post("/travel/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truckPlate").value("30A-99999"))
                .andExpect(jsonPath("$.scheduleName").value("Ha Noi - Hai Phong"));
    }

    /* ================= GET BY ID ================= */

    @Test
    @WithMockUser(username = "admin")
    void getTravelById_success() throws Exception {

        String response = mockMvc.perform(
                        post("/travel/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String travelId = objectMapper.readTree(response).get("travelId").asText();

        mockMvc.perform(
                        get("/travel/getById/{id}", travelId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelId").value(travelId));
    }

    /* ================= GET ALL ================= */

    @Test
    @WithMockUser(username = "admin")
    void getAllTravels_success() throws Exception {

        mockMvc.perform(
                post("/travel/created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
        );

        mockMvc.perform(
                        get("/travel/getAll")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    /* ================= UPDATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void updateTravel_success() throws Exception {

        String response = mockMvc.perform(
                        post("/travel/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String travelId = objectMapper.readTree(response).get("travelId").asText();

        TravelRequest update = new TravelRequest(
                truck.getId(),
                schedule.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );

        mockMvc.perform(
                        put("/travel/updated/{id}", travelId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").exists());
    }

    /* ================= DELETE ================= */

    @Test
    @WithMockUser(username = "admin")
    void deleteTravel_success() throws Exception {

        String response = mockMvc.perform(
                        post("/travel/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String travelId = objectMapper.readTree(response).get("travelId").asText();

        mockMvc.perform(
                        delete("/travel/deleted/{id}", travelId)
                )
                .andExpect(status().isOk());
    }
}