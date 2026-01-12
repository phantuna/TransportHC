package org.example.webapplication.Repository;

import org.example.webapplication.Dto.request.UserRequest;
import org.example.webapplication.Entity.User;
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
    List<User> findAllByRoleId(@Param("roleId") String roleId);
}
