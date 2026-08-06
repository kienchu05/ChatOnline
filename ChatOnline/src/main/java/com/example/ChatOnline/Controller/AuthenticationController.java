package com.example.ChatOnline.Controller;

import com.example.ChatOnline.DTO.Request.LoginRequest;
import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.DTO.Response.LoginResponse;
import com.example.ChatOnline.Service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/api/v1/auth/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest){
        var data = authenticationService.login(loginRequest);

        return ApiResponse.<LoginResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Login successfully")
                .data(data)
                .build();
    }
}
