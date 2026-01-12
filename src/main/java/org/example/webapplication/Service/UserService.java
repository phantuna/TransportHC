package org.example.webapplication.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Dto.request.UserRequest;
import org.example.webapplication.Dto.response.UserResponse;
import org.example.webapplication.Entity.Role_Permission.Role;
import org.example.webapplication.Entity.User;
import org.example.webapplication.Exception.AppException;
import org.example.webapplication.Exception.ErrorCode;
import org.example.webapplication.Repository.RoleRepository;
import org.example.webapplication.Repository.UserRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserResponse createdUser(UserRequest dto,String roleId){
        if(userRepository.existsByUsername(dto.getUsername())){
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


        User saved =  userRepository.save(driver);

        return UserResponse .builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .password(saved.getPassword())
                .birthday(saved.getBirthday())
                .phone(saved.getPhone())
                .roleIds(
                        saved.getRoles()
                                .stream()
                                .map(Role::getId)
                                .toList()
                )
                .baseSalary(saved.getBaseSalary())
                .build();
    }
    @PreAuthorize("isAuthenticated()")
    public UserResponse getUserById() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // username được set từ JWT

        User saved = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .password(saved.getPassword())
                .phone(saved.getPhone())
                .birthday(saved.getBirthday())
                .roleIds(saved.getRoles().stream().map(Role::getId).toList())
                .baseSalary(saved.getBaseSalary())
                .build();
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
    public List<UserResponse> getAllUsers(){
        List<UserResponse> userResponse = new ArrayList<>();
        List<User> users = userRepository.findAllByRoleId("R_DRIVER");
        for (User user : users) {
            UserResponse response =  UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .birthday(user.getBirthday())
                    .phone(user.getPhone())
                    .roleIds(user.getRoles()
                            .stream()
                            .map(Role::getId)
                            .toList())
                    .baseSalary(user.getBaseSalary())
                    .build();

            userResponse.add(response);
        }
        return userResponse;
    }


    @PreAuthorize("hasAuthority('MANAGE_USER')")
    @Transactional
    public void deleteUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
