package com.example.ChatOnline.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublicKeyRequest {

    @NotBlank
    private String publicKey;

    @NotBlank
    private String algorithm;
}