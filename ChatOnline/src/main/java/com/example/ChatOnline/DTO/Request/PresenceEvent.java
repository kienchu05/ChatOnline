package com.example.ChatOnline.DTO.Request;

import lombok.Builder;

@Builder
public record PresenceEvent (
        String userId,
        Boolean isOnline,
        String lastOnlineAt
){

}
