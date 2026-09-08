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
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserSessionService userSessionService;

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        //Lấy thông tin user và sessionId từ Websocket message
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Principal user = accessor.getUser();
        if (user == null || user.getName() == null) {
            log.warn("WebSocket connect nhưng không xác định được user");
            return;
        }

        String userId = user.getName();
        String sessionId = accessor.getSessionId();
        userSessionService.saveSession(userId, sessionId);

        // online db
        userSessionService.markOnline(userId);
        log.info("User {} ONLINE - session {}",userId,sessionId);
        // BROADCAST PRESENCE
        PresenceEvent presenceEvent =
                PresenceEvent.builder()
                        .userId(userId)
                        .isOnline(true)
                        .lastOnlineAt(null)
                        .build();
        messagingTemplate.convertAndSend("/topic/presence", presenceEvent);
    }

    // DISCONNECT
    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();

        if (user == null) {
            log.warn("WebSocket disconnect nhưng không có Principal");
            return;
        }

        String userId = user.getName();
        String sessionId = accessor.getSessionId();
        log.info("User {} DISCONNECTED - session {}",userId, sessionId);

        userSessionService.removeSession(userId, sessionId);
        boolean online = userSessionService.isOnline(userId);
        if (!online) {
            log.info("User {} OFFLINE", userId);
            // DB -> OFFLINE
            userSessionService.markOffline(userId);
            // Broadcast
            PresenceEvent presenceEvent =
                    PresenceEvent.builder()
                            .userId(userId)
                            .isOnline(false)
                            .lastOnlineAt(Instant.now())
                            .build();

            messagingTemplate.convertAndSend("/topic/presence",presenceEvent);
        } else {
            log.info("User {} vẫn còn session WebSocket khác",userId);
        }
    }
}