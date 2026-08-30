package com.example.ChatOnline.Configuration;

import com.example.ChatOnline.Entity.User;
import com.example.ChatOnline.Repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AccountLockFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String userId = authentication.getName();
            User user = userRepository.findById(userId)
                    .orElse(null);
            if (user != null && isLocked(user)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                        "code": 403,
                        "message": "Tài khoản đang bị khóa"
                    }
                    """);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isLocked(User user) {
        if (!user.isLocked()) {
            return false;
        }
        if (user.getLockedUntil() != null
                && LocalDateTime.now().isAfter(user.getLockedUntil())) {
            user.setLocked(false);
            user.setLockedUntil(null);
            userRepository.save(user);
            return false;
        }
        return true;
    }
}
