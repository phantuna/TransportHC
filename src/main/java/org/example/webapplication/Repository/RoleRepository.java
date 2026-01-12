package org.example.webapplication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.webapplication.Entity.Role_Permission.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,String> {

    Optional<Role> findByName(String name);
}
