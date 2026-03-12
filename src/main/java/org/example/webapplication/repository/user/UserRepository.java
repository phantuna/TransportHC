package org.example.webapplication.repository.user;

import org.example.webapplication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String>, UserRepositoryCustom {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    long countByRoles_Id(String roleId);
    List<User> findAllByRoles_Id(String roleId);
}
