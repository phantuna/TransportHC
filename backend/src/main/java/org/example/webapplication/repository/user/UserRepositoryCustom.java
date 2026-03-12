package org.example.webapplication.repository.user;

import org.example.webapplication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserRepositoryCustom {
    Page<User> findAllByRole_Id(String roleId, Pageable pageable);
    List<User> findUsersByRoleId(String roleId);
    List<User> findAvailableDrivers(String currentTruckId);
}
