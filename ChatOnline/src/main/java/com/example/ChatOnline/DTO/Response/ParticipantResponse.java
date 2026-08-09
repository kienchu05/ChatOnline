package com.example.ChatOnline.DTO.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ParticipantResponse {
    private String userId;
    private String username;
}
