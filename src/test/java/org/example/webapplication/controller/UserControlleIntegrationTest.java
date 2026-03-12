package org.example.webapplication.controller;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.example.webapplication.BaseMySQLIntegrationTest;
import org.example.webapplication.dto.request.user.UpdateUserRequest;
import org.example.webapplication.dto.request.user.UserRequest;
import org.example.webapplication.dto.response.user.UserResponse;
import org.example.webapplication.entity.Role_Permission.Permission;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.entity.User;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.repository.PermissionRepository;
import org.example.webapplication.repository.RoleRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class UserControlleIntegrationTest extends BaseMySQLIntegrationTest {

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;
    private UserRequest request;
    private UserResponse response;
    private Date birthDate;
    @Autowired
    private RoleRepository  roleRepository;
    @Autowired
    ObjectMapper objectMapper;


    @BeforeEach
    void initData() {
        seedPermission(PermissionKey.MANAGE, PermissionType.USER);
        seedPermission(PermissionKey.VIEW, PermissionType.USER);
        seedRole("R_ADMIN", "ADMIN");
        seedRole("R_MANAGER", "MANAGER");
        seedRole("R_DRIVER", "DRIVER");
        seedRole("R_ACCOUNTANT", "ACCOUNTANT");
        seedAdminRoleWithPermission();
        seedAdminUser();

        birthDate = Date.from(
                LocalDate.of(1990, 1, 1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );

        request = UserRequest.builder()
                .username(("john"))
                .password("123456")
                .phone("0324567890")
                .birthday(birthDate)
                .build();

        response = UserResponse.builder()
                .id("e35d6ccf-7461-4b4f-b07b-d84b")
                .username(("john"))
                .password("123456")
                .phone("0324567890")
                .birthday(birthDate)
                .baseSalary(5000000)
                .build();
    }

    @Test
    void createdUser_validRequest_success() throws Exception {
        //GIVEN
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(request);

        //WHEN,THEN
        var response = mockMvc.perform(MockMvcRequestBuilders
                        .post("/user/driver/created")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("john"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value("0324567890")
                );
        log.info("Rssult: {}", response.andReturn().getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "admin")
    void createManager_authenticated_success() throws Exception {

        UserRequest managerRequest = UserRequest.builder()
                .username("manager1")
                .password("123456")
                .phone("0324567890")
                .birthday(birthDate)
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/user/manager/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(managerRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("manager1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roleIds[0]").value("R_MANAGER"));
    }

    @Test
    @WithMockUser(username = "admin")
    void createAccount_authenticated_success() throws Exception {

        UserRequest managerRequest = UserRequest.builder()
                .username("account1")
                .password("123456")
                .phone("0324567890")
                .birthday(birthDate)
                .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/user/accountant/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(managerRequest))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("account1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roleIds[0]").value("R_ACCOUNTANT"));
    }

    @Test
    @WithMockUser(username = "admin")
    void getAllUsers_authenticated_success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String content = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/user/getAll")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray());

    }
    @Test
    @WithMockUser(username = "admin")
    void updateProfile_authenticated_success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        UpdateUserRequest update = UpdateUserRequest.builder()
                .phone("0888888888")
                .build();
        String content = objectMapper.writeValueAsString(update);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/user/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value("0888888888"));
    }

}

