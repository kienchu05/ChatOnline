package com.example.ChatOnline.Controller;

import com.example.ChatOnline.DTO.Request.CreateUserRequest;
import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.DTO.Response.CreateUserResponse;
import com.example.ChatOnline.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/api/v1/users")
    public ApiResponse<CreateUserResponse> createUser(@RequestBody CreateUserRequest createUserRequest){
        var data = userService.createUser(createUserRequest);

        return ApiResponse.<CreateUserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("User created successfully !")
                .data(data)
                .build();
    }
}
