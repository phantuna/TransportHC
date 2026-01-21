package org.example.webapplication.repository;

import org.example.webapplication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("""
        select distinct u
        from User u
        join u.roles r
        where r.id = :roleId
    """)
    Page<User> findAllByRole_Id(String roleId, Pageable pageable);
}
