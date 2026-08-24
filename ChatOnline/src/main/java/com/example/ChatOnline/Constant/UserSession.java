package com.example.ChatOnline.Constant;

import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserSession {
    private String userId;
    private String sessionId;
    private Instant connectedAt;
}
