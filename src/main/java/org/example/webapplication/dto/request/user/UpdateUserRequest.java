package org.example.webapplication.dto.request.user;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class UpdateUserRequest {
    private String username;
    private String phone;
    private Date birthday;
}
