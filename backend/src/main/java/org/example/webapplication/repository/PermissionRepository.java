package org.example.webapplication.repository;

import org.example.webapplication.entity.Role_Permission.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
    @Override
    boolean existsById(String id);
}
