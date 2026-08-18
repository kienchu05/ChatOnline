package com.example.ChatOnline.DTO.Response;

import lombok.Builder;

@Builder
public record UserDetailResponse(
        String userId,
        String email,
        String username
) {
}