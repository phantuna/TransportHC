package org.example.webapplication.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.webapplication.BaseMySQLIntegrationTest;
import org.example.webapplication.dto.request.schedule.ScheduleRequest;
import org.example.webapplication.enums.ApprovalStatus;
import org.example.webapplication.entity.Role_Permission.Permission;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.repository.PermissionRepository;
import org.example.webapplication.repository.RoleRepository;
import org.example.webapplication.repository.schedule.ScheduleRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ScheduleControllerIntegration extends BaseMySQLIntegrationTest {
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
    ScheduleRepository scheduleRepository;

    private ScheduleRequest validRequest;

    @BeforeEach
    void setUp() {
        // permission cho schedule
        seedPermission(PermissionKey.CREATE, PermissionType.SCHEDULE);
        seedPermission(PermissionKey.UPDATE, PermissionType.SCHEDULE);
        seedPermission(PermissionKey.VIEW, PermissionType.SCHEDULE);
        seedPermission(PermissionKey.MANAGE, PermissionType.SCHEDULE);
        seedPermission(PermissionKey.APPROVE, PermissionType.SCHEDULE);
        seedPermission(PermissionKey.UPDATE, PermissionType.SCHEDULE_DOCUMENT);

        seedRole("R_ADMIN", "ADMIN");

        Role admin = roleRepository.findById("R_ADMIN").orElseThrow();
        admin.setPermissions(permissionRepository.findAll());
        roleRepository.save(admin);

        validRequest = new ScheduleRequest(
                "HaNoi",
                "HaiPhong",
                500000,
                "Test schedule"
        );
    }

    /* ================= CREATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void createSchedule_success() throws Exception {

        mockMvc.perform(
                        post("/schedule/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startPlace").value("HaNoi"))
                .andExpect(jsonPath("$.endPlace").value("HaiPhong"))
                .andExpect(jsonPath("$.approval").value("PENDING_APPROVAL"));
    }

    /* ================= GET ALL ================= */

    @Test
    @WithMockUser(username = "admin")
    void getAllSchedule_success() throws Exception {

        mockMvc.perform(
                post("/schedule/created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
        );

        mockMvc.perform(
                        get("/schedule/getAll")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    /* ================= UPDATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void updateSchedule_success() throws Exception {

        String response = mockMvc.perform(
                        post("/schedule/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String scheduleId = objectMapper.readTree(response).get("id").asText();

        ScheduleRequest update = new ScheduleRequest(
                "HaNoi",
                "DaNang",
                800000,
                "Updated"
        );

        mockMvc.perform(
                        put("/schedule/updated/{id}", scheduleId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endPlace").value("DaNang"))
                .andExpect(jsonPath("$.expense").value(800000));
    }

    /* ================= APPROVAL ================= */

    @Test
    @WithMockUser(username = "admin")
    void approveSchedule_success() throws Exception {

        String response = mockMvc.perform(
                        post("/schedule/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String scheduleId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(
                        post("/schedule/approval/{id}", scheduleId)
                                .param("status", ApprovalStatus.APPROVED.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approval").value("APPROVED"));
    }

    /* ================= UPLOAD DOCUMENT ================= */

    @Test
    @WithMockUser(username = "admin")
    void uploadDocument_success() throws Exception {

        String response = mockMvc.perform(
                        post("/schedule/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String scheduleId = objectMapper.readTree(response).get("id").asText();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        mockMvc.perform(
                        multipart("/schedule/{id}/documents", scheduleId)
                                .file(file)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.pdf"))
                .andExpect(jsonPath("$.scheduleId").value(scheduleId));
    }

    /* ================= DELETE ================= */

    @Test
    @WithMockUser(username = "admin")
    void deleteSchedule_success() throws Exception {

        String response = mockMvc.perform(
                        post("/schedule/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        String scheduleId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(
                        delete("/schedule/deleted/{id}", scheduleId)
                )
                .andExpect(status().isOk());
    }
}
