package org.example.webapplication.dto.request.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthenticationRequest {
    @NotBlank(message = "USERNAME_NOT_NULL")
    @Size (min = 3,message ="USERNAME_NOT_VALID")
    private String username;

    @NotBlank(message = "PASSWORD_NOT_NULL")
    @Size(min = 5, message = "PASSWORD_NOT_VALID")
    private String password;
}
