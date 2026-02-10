package org.example.webapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.user.UpdateUserRequest;
import org.example.webapplication.dto.request.user.UserRequest;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.dto.response.user.UserResponse;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.entity.User;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.RoleRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.example.webapplication.service.cache.UserCacheService;
import org.example.webapplication.service.mapper.UserMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;
    private final UserCacheService userCacheService;
    private final UserMapper  userMapper;


    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    @CacheEvict(value = "users_list", allEntries = true)
    @Transactional
    public UserResponse createdUser(UserRequest dto,String roleId) {
        if (!"R_DRIVER".equals(roleId)) {
            permissionService.getUser(
                    List.of(PermissionKey.MANAGE),
                    PermissionType.USER
            );
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User driver = new User();
        driver.setUsername(dto.getUsername());
        driver.setBirthday(dto.getBirthday());
        driver.setPhone(dto.getPhone());
        driver.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role driverRole = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.ROLE_NOT_FOUND));
        driver.setRoles(List.of(driverRole));
        User saved = userRepository.save(driver);
        return userMapper.toResponse(saved);
    }

    public UserResponse getUserById() {
        User currentUser = permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.USER
        );
        return userMapper.toResponse(currentUser);
    }

    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.USER
        );
        return userCacheService.getAllUsers(page, size);
    }




    @CacheEvict(value = "users_list", allEntries = true)
    @Transactional
    public UserResponse updateMyProfile(UpdateUserRequest dto) {

        User currentUser = permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.USER
        );

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            currentUser.setUsername(dto.getUsername());
        }

        if (dto.getPhone() != null) {
            currentUser.setPhone(dto.getPhone());
        }

        if (dto.getBirthday() != null) {
            currentUser.setBirthday(dto.getBirthday());
        }

        User saved = userRepository.save(currentUser);
        return userMapper.toResponse(saved);
    }

    @CacheEvict(value = "users_list", allEntries = true)
    @Transactional
    public void deleteUserById(String id) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.USER
        );
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
