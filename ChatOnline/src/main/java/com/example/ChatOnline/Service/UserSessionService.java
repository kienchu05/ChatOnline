package com.example.ChatOnline.Service;

import com.example.ChatOnline.Constant.UserPresence;
import com.example.ChatOnline.Constant.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private static final String SESSION_PREFIX  = "ws:session:";
    private static final String USER_SESSIONS   = "ws:user:%s:sessions";
    private static final String PRESENCE_PREFIX = "ws:presence:";

    private static final Duration SESSION_TTL  = Duration.ofHours(24);
    private static final Duration PRESENCE_TTL = Duration.ofDays(30);

    private final RedisTemplate<String, Object> redisTemplate;
    private final JsonMapper jsonMapper;

    public void saveSession(String userId, String sessionId) {
        // Tạo UserSession object với thông tin session
        UserSession userSession = UserSession.builder()
                .userId(userId)
                .sessionId(sessionId)
                .connectedAt(Instant.now())
                .build();

        // Lưu session vào Redis với key "ws:session:{sessionId}" và TTL 24h
        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, userSession, SESSION_TTL);

        // Tạo key để lưu tất cả sessions của user: "ws:user:{userId}:sessions"
        String userSessionsKey = String.format(USER_SESSIONS, userId);

        // Thêm sessionId vào Set sessions của user (hỗ trợ multi-device)
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        // Set TTL cho user sessions set
        redisTemplate.expire(userSessionsKey, SESSION_TTL);
    }

    public void removeSession(String userId, String sessionId) {
        // Xóa session khỏi Redis
        redisTemplate.delete(SESSION_PREFIX + sessionId);

        // Xóa sessionId khỏi Set sessions của user
        String userSessionKey = String.format(USER_SESSIONS, userId);
        redisTemplate.opsForSet().remove(userSessionKey, sessionId);

        // Kiểm tra nếu user không còn session nào (offline hoàn toàn)
        if(!isOnline(userId)) {
            // Lưu thời gian last online vào presence
            UserPresence userPresence = UserPresence.builder()
                    .userId(userId)
                    .lastOnlineAt(Instant.now())
                    .build();

            redisTemplate.opsForValue().set(PRESENCE_PREFIX + userId, userPresence, PRESENCE_TTL);
        }
    }

    public boolean isOnline(String userId) {
        // Lấy key chứa tất cả sessions của user
        String userSessionsKey = String.format(USER_SESSIONS, userId);
        // Đếm số sessions trong Set
        Long size = redisTemplate.opsForSet().size(userSessionsKey);
        // User online nếu có ít nhất 1 session
        return size != null && size > 0;
    }

    public Optional<UserPresence> getPresence(String userId) {
        // Lấy presence data từ Redis
        Object raw = redisTemplate.opsForValue().get(PRESENCE_PREFIX + userId);
        if (raw == null) return Optional.empty();

        // Convert raw object thành UserPresence
        return Optional.of(jsonMapper.convertValue(raw, UserPresence.class));
    }
}
