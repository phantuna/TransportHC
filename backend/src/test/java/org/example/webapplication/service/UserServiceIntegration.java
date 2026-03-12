package org.example.webapplication.service;

import jakarta.transaction.Transactional;
import org.example.webapplication.BaseMySQLIntegrationTest;
import org.example.webapplication.dto.request.user.UpdateUserRequest;
import org.example.webapplication.dto.request.user.UserRequest;
import org.example.webapplication.dto.response.user.UserResponse;
import org.example.webapplication.entity.User;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Testcontainers
class UserServiceIntegration extends BaseMySQLIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // permission USER
        seedPermission(PermissionKey.MANAGE, PermissionType.USER);
        seedPermission(PermissionKey.VIEW, PermissionType.USER);
        seedRole("R_DRIVER", "DRIVER");
        seedAdminRoleWithPermission();
        seedAdminUser();
    }

    /* ================= CREATE ================= */

    @Test
    @WithMockUser(username = "admin")
    void createDriver_success() {

        UserRequest request = UserRequest.builder()
                .username("driver1")
                .password("123456")
                .phone("0123456789")
                .build();

        UserResponse response =
                userService.createdUser(request, "R_DRIVER");

        assertThat(response.getUsername()).isEqualTo("driver1");
        assertThat(response.getRoleIds()).contains("R_DRIVER");
    }

    @Test
    @WithMockUser(username = "admin")
    void createUser_duplicateUsername_fail() {

        UserRequest request = UserRequest.builder()
                .username("dup")
                .password("123456")
                .build();

        userService.createdUser(request, "R_DRIVER");

        assertThatThrownBy(() ->
                userService.createdUser(request, "R_DRIVER")
        ).isInstanceOf(AppException.class)
                .hasMessageContaining("USER_EXISTED");
    }

    /* ================= GET CURRENT USER ================= */

    @Test
    @WithMockUser(username = "admin")
    void getUserById_success() {

        UserResponse response = userService.getUserById();

        assertThat(response.getUsername()).isEqualTo("admin");
    }

    /* ================= UPDATE PROFILE ================= */

    @Test
    @WithMockUser(username = "admin")
    void updateMyProfile_success() {

        UpdateUserRequest update = UpdateUserRequest.builder()
                .phone("0999999999")
                .build();

        UserResponse response =
                userService.updateMyProfile(update);

        assertThat(response.getPhone()).isEqualTo("0999999999");
    }

    /* ================= DELETE ================= */

    @Test
    @WithMockUser(username = "admin")
    void deleteUser_success() {

        User user = new User();
        user.setUsername("delete_me");
        user.setPassword("123456");
        user = userRepository.save(user);

        userService.deleteUserById(user.getId());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

}
