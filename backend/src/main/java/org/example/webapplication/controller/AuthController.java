package org.example.webapplication.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.authentication.AuthenticationRequest;
import org.example.webapplication.dto.response.authentication.ApiResponse;
import org.example.webapplication.dto.response.authentication.AuthenticationResponse;
import org.example.webapplication.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private final AuthenticationService authenticationService;

    @PostMapping("")
    ApiResponse<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {

        ApiResponse< AuthenticationResponse> response = new ApiResponse<>();
        response.setResult(authenticationService.authenticate(request));
        return response;
    }
}
