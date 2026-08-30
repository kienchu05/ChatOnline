package com.example.ChatOnline.DTO.Response;

import java.time.Instant;

public record AdminUserResponse(
        String userId,
        String username,
        String email,
        boolean isOnline,
        Instant lastOnlineAt,
        boolean isLooked
) {
}
