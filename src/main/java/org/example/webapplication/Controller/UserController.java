package org.example.webapplication.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Dto.request.User.UserRequest;
import org.example.webapplication.Dto.response.User.UserResponse;
import org.example.webapplication.Service.UserService;
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
    @PostAuthorize("hasAuthority('MANAGE_USER')")
    @PostMapping("/accountant/created")
    public UserResponse createdAccountant(@RequestBody @Valid UserRequest request) {
        return userService.createdUser(request, "R_ACCOUNTANT");
    }
    @PostAuthorize("hasAuthority('MANAGE_USER')")
    @PostMapping("/manager/created")
    public UserResponse createdManager(@RequestBody @Valid UserRequest request) {
        return userService.createdUser(request, "R_MANAGER");
    }
    @PreAuthorize("hasAuthority('MANAGE_USER') OR hasAuthority('VIEW_USER')  " )
    @GetMapping("/driver/view")
    public UserResponse viewDriver() {

        return userService.getUserById();
    }

    @PreAuthorize("hasAuthority('MANAGE_USER')  ")
    @GetMapping("/getAll")
    public List<UserResponse> getAllUser() {
        return userService.getAllUsers();
    }


    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USER')  ")
    public void deleteUser(@NotBlank @PathVariable String id) {
         userService.deleteUserById(id);
    }

}
