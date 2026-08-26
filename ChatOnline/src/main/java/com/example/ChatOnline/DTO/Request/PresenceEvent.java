package com.example.ChatOnline.DTO.Request;

import lombok.Builder;

import java.time.Instant;

@Builder
public record PresenceEvent (
        String userId,
        Boolean isOnline,
        Instant lastOnlineAt
){

}
