package org.example.webapplication.Dto.request.User;

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
    @NotEmpty(message = "USERNAME_NOT_NULL")
    @Size(min = 3,message ="USERNAME_NOT_VALID")
    private String username;

    @NotEmpty(message = "PASSWORD_NOT_NULL")
    @Min(value = 5, message = "PASSWORD_NOT_VALID")
    private String password;

    @Pattern(
            regexp = "^(0[0-9]{9})?$",
            message = "PHONE_NOT_VALID"
    )
    private String phone;

    @Past(message = "DATE_IN_PAST")
    private Date birthday;

    private List<String > roleIds;

}

