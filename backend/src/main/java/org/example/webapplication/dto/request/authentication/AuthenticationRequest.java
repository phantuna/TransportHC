package org.example.webapplication.dto.request.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthenticationRequest {
    @NotBlank(message = "validation.username.not_null")
    @Size (min = 3,message ="validation.username.invalid")
    private String username;

    @NotBlank(message = "validation.password.not_null")
    @Size(min = 5, message = "validation.password.invalid")
    private String password;
}
