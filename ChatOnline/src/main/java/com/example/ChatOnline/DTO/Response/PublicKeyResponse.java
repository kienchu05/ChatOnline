package com.example.ChatOnline.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicKeyResponse {

    private String userId;

    private String publicKey;

    private String algorithm;
}
