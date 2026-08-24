package com.example.ChatOnline.DTO.Request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserStatus {
    private String userId;
    private boolean isOnline;
    private LocalDateTime lastSeen;
}
