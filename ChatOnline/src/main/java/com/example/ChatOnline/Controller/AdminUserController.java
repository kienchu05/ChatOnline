package com.example.ChatOnline.Controller;

import com.example.ChatOnline.DTO.Response.AdminUserResponse;
import com.example.ChatOnline.Service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasRole('ADMIN_ROLE')")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping("/api/v1/admin/users")
    public List<AdminUserResponse> getUsers() {
        return adminUserService.getAllUsers();
    }

    @PutMapping("/api/v1/admin/users/{userId}/lock")
    public ResponseEntity<?> lockUser(
            @PathVariable String userId
    ) {
        adminUserService.lockUser(userId, 30);
        return ResponseEntity.ok(
                Map.of(
                        "code", 200,
                        "message", "Khóa tài khoản thành công"
                )
        );
    }

    @PutMapping("/api/v1/admin/users/{userId}/unlock")
    public ResponseEntity<?> unlockUser(
            @PathVariable String userId
    ) {
        adminUserService.unlockUser(userId);
        return ResponseEntity.ok(
                "Mở khóa tài khoản thành công"
        );
    }
}
