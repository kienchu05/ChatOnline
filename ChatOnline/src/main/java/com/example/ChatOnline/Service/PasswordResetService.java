package com.example.ChatOnline.Service;

import com.example.ChatOnline.Entity.PasswordReset;
import com.example.ChatOnline.Entity.User;
import com.example.ChatOnline.Enum.ErrorCode;
import com.example.ChatOnline.Exception.AppException;
import com.example.ChatOnline.Repository.PasswordResetRepository;
import com.example.ChatOnline.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetRepository resetRepository;
    private final MailService service;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String sendOtp(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        resetRepository.deleteByUserId(user.getId());

        String otp = String.format("%06d", new Random().nextInt(100000));

        PasswordReset passwordReset = PasswordReset.builder()
                .otp(otp)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();

        resetRepository.save(passwordReset);

        service.sendOtp(email ,otp);

        log.info("OTP mới cho user {}: {}", user.getId(), otp);

        return "Đã gửi mã OTP !";
    }

    public String verifyOtp(String email, String otp){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        PasswordReset passwordReset = resetRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

        if(passwordReset.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("OTP đã hết hạn !");
        }

        if(!passwordReset.getOtp().equals(otp)){
            throw new RuntimeException("OTP không chính xác");
        }

        passwordReset.setVerified(true);
        resetRepository.save(passwordReset);
        return "OTP hợp lệ !";
    }

    @Transactional
    public String resetPassword(String email, String newPassword){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        PasswordReset passwordReset = resetRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new RuntimeException("OTP không tồn tại !"));

        if(!Boolean.TRUE.equals(passwordReset.getVerified())){
            throw new RuntimeException("Bạn chưa xác thực OTP !");
        }

        if(passwordReset.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("OTP đã hết hạn !");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetRepository.delete(passwordReset);
        return "Đổi mật khẩu thành công !";
    }
}
