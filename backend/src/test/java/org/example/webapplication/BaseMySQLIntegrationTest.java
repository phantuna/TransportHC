package org.example.webapplication;

import org.example.webapplication.entity.Role_Permission.Permission;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.entity.User;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.repository.PermissionRepository;
import org.example.webapplication.repository.RoleRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.mysql.MySQLContainer;

import java.util.ArrayList;
import java.util.List;

public abstract  class BaseMySQLIntegrationTest {
    @Autowired
    PermissionRepository permissionRepository;
    @Autowired

    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Container
    static final org.testcontainers.mysql.MySQLContainer MY_SQL_CONTAINER = new MySQLContainer("mysql:8.0.44-debian");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", ()->MY_SQL_CONTAINER.getJdbcUrl());
        registry.add("spring.datasource.username", ()->MY_SQL_CONTAINER.getUsername());
        registry.add("spring.datasource.password", ()->MY_SQL_CONTAINER.getPassword());
        registry.add("spring.datasource.driver-class-name",()->"com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto",()->"update");

    }
    public void seedPermission(PermissionKey key, PermissionType type) {
        String id = "P_" + type.name() + "_" + key.name();
        if (!permissionRepository.existsById(id)) {
            Permission p = new Permission();
            p.setId(id);
            p.setPermission_key(key);
            p.setPermission_type(type);
            permissionRepository.save(p);
        }
    }

    public void seedRole(String id, String name) {
        if (!roleRepository.existsById(id)) {
            Role r = new Role();
            r.setId(id);
            r.setName(name);
            roleRepository.save(r);
        }
    }
    public void seedAdminRoleWithPermission() {
        Role adminRole = roleRepository.findById("R_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setId("R_ADMIN");
                    r.setName("ADMIN");
                    return roleRepository.save(r);
                });

        Permission manageUser = permissionRepository
                .findById("P_USER_MANAGE")
                .orElseThrow();
        Permission viewUser = permissionRepository
                .findById("P_USER_VIEW")
                .orElseThrow();

        adminRole.setPermissions(
                new ArrayList<>(List.of(manageUser, viewUser))
        );

        roleRepository.save(adminRole);
    }

    public void seedAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRoles(List.of(
                    roleRepository.findById("R_ADMIN").orElseThrow()
            ));
            userRepository.save(admin);
        }
    }
}
