package org.example.webapplication.repository.user;

import org.example.webapplication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> findAllByRole_Id(String roleId, Pageable pageable);

}
