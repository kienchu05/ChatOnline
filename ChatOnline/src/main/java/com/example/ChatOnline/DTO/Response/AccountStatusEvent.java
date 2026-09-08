package com.example.ChatOnline.DTO.Response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusEvent {
    private String type;
    private String message;
    private LocalDateTime lockedUntil;
}
