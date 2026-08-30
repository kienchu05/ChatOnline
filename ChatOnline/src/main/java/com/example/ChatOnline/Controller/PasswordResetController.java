package com.example.ChatOnline.Controller;

import com.example.ChatOnline.DTO.Request.ForgotPasswordRequest;
import com.example.ChatOnline.DTO.Request.ResetPasswordRequest;
import com.example.ChatOnline.DTO.Request.VerifyOtpRequest;
import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.Service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService service;

    @PostMapping("/api/v1/passwd/forgot-password")
    public ApiResponse<?> forgotPassword(@RequestBody ForgotPasswordRequest request){
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Gửi mã xác thực thành công")
                .data(service.sendOtp(request.getEmail()))
                .build();
    }

    @PostMapping("/api/v1/passwd/verify-otp")
    public ApiResponse<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        var data = service.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Xác minh mã OTP thành công !")
                .data(data)
                .build();
    }

    @PostMapping("/api/v1/passwd/reset-password")
    public ApiResponse<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        var data = service.resetPassword(
                request.getEmail(),
                request.getNewPassword());
        return ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .data(data)
                .message("Đặt lại mật khẩu thành công !")
                .build();
    }
}
