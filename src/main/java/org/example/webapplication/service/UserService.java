package org.example.webapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.user.UserRequest;
import org.example.webapplication.dto.response.user.UserResponse;
import org.example.webapplication.entity.Role_Permission.Role;
import org.example.webapplication.entity.User;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.RoleRepository;
import org.example.webapplication.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
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

    public UserResponse createdUser(UserRequest dto,String roleId) {
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

        return toResponse(saved);
    }
    @PreAuthorize("isAuthenticated()")
    public UserResponse getUserById() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // username được set từ JWT

        User saved = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return toResponse(saved);
    }

//    @PostAuthorize("returnObject.username == authentication.name")
//    public UserResponse getUserById(String id){
//        User saved = userRepository.getById(id);
//        return UserResponse.builder()
//                .id(saved.getId())
//                .username(saved.getUsername())
//                .password(saved.getPassword())
//                .phone(saved.getPhone())
//                .birthday(saved.getBirthday())
//                .roleIds(
//                        saved.getRoles()
//                                .stream()
//                                .map(Role::getId)
//                                .toList()
//                )
//                .baseSalary(saved.getBaseSalary())
//                .build();
//    }

    @PreAuthorize("hasAuthority('MANAGE_USER') " )
    public Page<UserResponse> getAllUsers(int page , int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAllByRole_Id("R_DRIVER",pageable);
        
        return usersPage.map(this::toResponse);

    }


    @PreAuthorize("hasAuthority('MANAGE_USER')")
    @Transactional
    public void deleteUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
