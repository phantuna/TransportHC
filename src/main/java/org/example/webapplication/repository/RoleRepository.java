package org.example.webapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.webapplication.entity.Role_Permission.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,String> {

    Optional<Role> findByName(String name);
    boolean existsById(String id);
}
