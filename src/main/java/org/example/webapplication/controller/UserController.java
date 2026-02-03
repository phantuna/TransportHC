package org.example.webapplication.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.user.UpdateUserRequest;
import org.example.webapplication.dto.request.user.UserRequest;
import org.example.webapplication.dto.response.user.UserResponse;
import org.example.webapplication.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;


    @PostMapping("/driver/created")
    public UserResponse createdDriver(@RequestBody @Valid UserRequest request) {
        return userService.createdUser(request, "R_DRIVER");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/accountant/created")
    public UserResponse createdAccountant(@RequestBody @Valid UserRequest request) {
        return userService.createdUser(request, "R_ACCOUNTANT");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/manager/created")
    public UserResponse createdManager(@RequestBody @Valid UserRequest request) {
        return userService.createdUser(request, "R_MANAGER");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/driver/view")
    public UserResponse viewDriver() {

        return userService.getUserById();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getAll")
    public Page<UserResponse> getAllUser( @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {

        return userService.getAllUsers(page,size);
    }

    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateMyProfile(
            @RequestBody UpdateUserRequest request
    ) {
        return userService.updateMyProfile(request);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteUser(@NotBlank @PathVariable String id) {
         userService.deleteUserById(id);
    }

}
