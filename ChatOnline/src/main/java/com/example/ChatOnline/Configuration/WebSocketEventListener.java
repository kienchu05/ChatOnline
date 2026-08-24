package com.example.ChatOnline.Configuration;
import com.example.ChatOnline.Constant.DateTimeUtils;
import com.example.ChatOnline.DTO.Request.PresenceEvent;
import com.example.ChatOnline.Service.UserSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserSessionService userSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor  = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if(user == null || user.getName() == null) {
            return;
        }

        String userId = user.getName();
        String sessionId = accessor.getSessionId();

        // 1. Lưu session vào Redis
        userSessionService.saveSession(userId, sessionId);
        log.info("User {} connected with session {}", userId, sessionId);

        // 2. Bắn sự kiện online tới /topic/presence
        PresenceEvent presenceEvent = PresenceEvent.builder()
                .userId(userId)
                .isOnline(true)
                .lastOnlineAt(null)
                .build();

        messagingTemplate.convertAndSend("/topic/presence", presenceEvent);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor  = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if(user == null || user.getName() == null) {
            return;
        }
        String userId = user.getName();
        String sessionId = accessor.getSessionId();
        // 1. Xóa session khỏi Redis
        userSessionService.removeSession(userId, sessionId);
        log.info("User {} disconnected with session {}", userId, sessionId);

        // 2. Chỉ broadcast offline khi user không còn session nào đang hoạt động
        if (!userSessionService.isOnline(userId)) {
            PresenceEvent presenceEvent = PresenceEvent.builder()
                    .userId(userId)
                    .isOnline(false)
                    .lastOnlineAt(DateTimeUtils.formatLastOnlineAt(Instant.now()))
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/presence",
                    presenceEvent
            );
        }}

}
