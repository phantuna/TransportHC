package org.example.webapplication.service.mapper;

import org.example.webapplication.dto.response.user.UserResponse;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .birthday(user.getBirthday())
                .phone(user.getPhone())
                .roleIds(
                        user.getRoles()
                                .stream()
                                .map(Role::getId)
                                .toList()
                )
                .baseSalary(user.getBaseSalary())
                .build();
    }
}
