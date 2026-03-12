package org.example.webapplication.dto.request.user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserRequest {
    @NotEmpty(message = "validation.username.not_null")
    @Size(min = 3,message ="validation.username.invalid")
    private String username;

    @NotEmpty(message = "validation.password.not_null")
    @Size(min = 5, message = "validation.password.invalid")
    private String password;

    @Pattern(
            regexp = "^(0[0-9]{9})?$",
            message = "validation.phone.invalid"
    )
    private String phone;

    @Past(message = "validation.date.in_past")
    private Date birthday;

    private List<String> roleIds;

}

