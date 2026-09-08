package com.example.ChatOnline.Service;

import com.example.ChatOnline.DTO.Response.AccountStatusEvent;
import com.example.ChatOnline.DTO.Response.AdminUserResponse;
import com.example.ChatOnline.Entity.User;
import com.example.ChatOnline.Enum.ErrorCode;
import com.example.ChatOnline.Exception.AppException;
import com.example.ChatOnline.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserSessionService userSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    boolean online = userSessionService.isOnline(user.getId());
                    Instant lastOnline = user.getLastSeen();
                    return new AdminUserResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            online,
                            lastOnline,
                            user.isLocked());
                }).toList();
    }

    @Transactional
    public void lockUser(String userId, int seconds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setLocked(true);
        user.setLockedUntil(
                LocalDateTime.now().plusSeconds(seconds)
        );
        userRepository.save(user);

        AccountStatusEvent event = AccountStatusEvent.builder()
                        .type("ACCOUNT_LOCKED")
                        .message("Tài khoản của bạn đã bị khóa")
                        .lockedUntil(user.getLockedUntil())
                        .build();

        messagingTemplate.convertAndSendToUser(userId, "/queue/account-status", event);
    }

    @Transactional
    public void unlockUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setLocked(false);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
}
