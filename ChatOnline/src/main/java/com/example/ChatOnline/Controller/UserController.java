package com.example.ChatOnline.Controller;

import com.example.ChatOnline.DTO.Request.CreateUserRequest;
import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.DTO.Response.CreateUserResponse;
import com.example.ChatOnline.DTO.Response.PageResponse;
import com.example.ChatOnline.DTO.Response.UserDetailResponse;
import com.example.ChatOnline.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api/v1/users")
    public ApiResponse<UserDetailResponse> myInfo(@AuthenticationPrincipal Jwt jwt) {
        // Extract userId từ JWT token subject
        var userId = jwt.getSubject();

        // Gọi service để lấy user info
        UserDetailResponse data = userService.myInfo(userId);

        return ApiResponse.<UserDetailResponse>builder()
                .code(HttpStatus.OK.value())
                .message("User info retrieved successfully")
                .data(data)
                .build();
    }

    @GetMapping("/api/v1/users/search")
    public ApiResponse<PageResponse<UserDetailResponse>> searchUsers(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "5") int size,
            @RequestParam(required = false) String keyword){
        var data = userService.searchUsers(keyword, page, size);

        return ApiResponse.<PageResponse<UserDetailResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Users retrieved successfully")
                .data(data)
                .build();
    }
}
