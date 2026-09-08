package com.example.ChatOnline.Service;

import com.example.ChatOnline.Constant.UserPresence;
import com.example.ChatOnline.Constant.UserSession;
import com.example.ChatOnline.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JsonMapper jsonMapper;

    private static final String SESSION_PREFIX = "ws:session:";
    private static final String USER_SESSIONS = "ws:user:%s:sessions";
    private static final String PRESENCE_PREFIX = "ws:presence:";

    private static final Duration SESSION_TTL =
            Duration.ofHours(24);

    private static final Duration PRESENCE_TTL =
            Duration.ofDays(30);

    public void saveSession(String userId, String sessionId) {
        UserSession userSession =
                UserSession.builder()
                        .userId(userId)
                        .sessionId(sessionId)
                        .connectedAt(Instant.now())
                        .build();

        // Lưu session
        redisTemplate.opsForValue().set(SESSION_PREFIX + getSessionId(sessionId), userSession, SESSION_TTL);
        String userSessionsKey = String.format(USER_SESSIONS, userId);
        // Thêm session vào Set
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        // TTL cho Set
        redisTemplate.expire(userSessionsKey, SESSION_TTL);
        // User online
        markOnline(userId);
    }

    private static String getSessionId(String sessionId) {
        return sessionId;
    }

    public void removeSession(String userId, String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        String userSessionsKey = String.format(USER_SESSIONS, userId);
        // 1. Xóa session
        redisTemplate.delete(sessionKey);
        // 2. Xóa session khỏi Set
        redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
        // 3. Kiểm tra còn session không
        if (!isOnline(userId)) {
            // DB OFFLINE
            markOffline(userId);
            // Redis presence
            UserPresence presence = UserPresence.builder()
                            .userId(userId)
                            .lastOnlineAt(Instant.now())
                            .build();
            redisTemplate.opsForValue().set(PRESENCE_PREFIX + userId, presence, PRESENCE_TTL);
        }
    }

    public boolean isOnline(String userId) {
        String userSessionsKey = String.format(USER_SESSIONS, userId);
        Set<Object> sessions = redisTemplate.opsForSet().members(userSessionsKey);

        if (sessions == null || sessions.isEmpty()) {
            return false;
        }

        boolean hasValidSession = false;
        for (Object session : sessions) {
            String sessionId = String.valueOf(session);
            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_PREFIX + sessionId));
            if (exists) {
                hasValidSession = true;
            } else {
                // Session đã hết hạn
                // nhưng ID vẫn còn trong Set
                redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
            }
        }
        return hasValidSession;
    }

    private void cleanupExpiredSessions(String userId) {

        String userSessionsKey = String.format(USER_SESSIONS, userId);
        Set<Object> sessions = redisTemplate.opsForSet().members(userSessionsKey);

        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (Object session : sessions) {
            String sessionId = String.valueOf(session);
            boolean exists = Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_PREFIX + sessionId));
            if (!exists) {
                redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
            }
        }
    }

    public Optional<UserPresence> getPresence(String userId) {
        Object raw = redisTemplate.opsForValue().get(PRESENCE_PREFIX + userId);
        if (raw == null) {
            return Optional.empty();
        }
        return Optional.of(jsonMapper.convertValue(raw, UserPresence.class)
        );
    }

    public void markOffline(String userId) {
        userRepository.findById(userId)
                .ifPresent(user -> {
                    user.setIsOnline(false);
                    user.setLastSeen(Instant.now());
                    userRepository.save(user);
                });
    }

    public void markOnline(String userId) {
        userRepository.findById(userId)
                .ifPresent(user -> {
                    user.setIsOnline(true);
                    user.setLastSeen(null);
                    userRepository.save(user);
                });
    }
}