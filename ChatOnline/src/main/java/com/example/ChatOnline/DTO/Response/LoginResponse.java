package com.example.ChatOnline.DTO.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.bridge.IMessage;

@Getter
@Setter
@Builder
public class LoginResponse {
    private String message;
    private int status;
    private String userId;
    private String accessToken;
    private String refreshToken;
}
