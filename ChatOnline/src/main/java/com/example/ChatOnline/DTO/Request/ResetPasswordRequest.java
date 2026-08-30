package com.example.ChatOnline.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    private String email;
    private String newPassword;
}
