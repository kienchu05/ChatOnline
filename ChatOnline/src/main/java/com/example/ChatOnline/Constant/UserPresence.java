package com.example.ChatOnline.Constant;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserPresence {
    private String userId;
    private Instant lastOnlineAt;
}

