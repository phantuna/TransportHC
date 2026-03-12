package org.example.webapplication.dto.request.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
public class UpdateUserRequest {
    private String username;
    private String phone;
    private Date birthday;
}
