package org.example.webapplication.dto.response.authentication;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class AuthenticationResponse {
    private boolean authenticated;
    private String token;
    private List<String> permissions;
}
