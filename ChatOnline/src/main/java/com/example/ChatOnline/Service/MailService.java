package com.example.ChatOnline.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;

    public void sendOtp(String email, String otp){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Mã xác nhận đặt lại mật khẩu");
        message.setText("Mã xác nhận của bạn là: " + otp + "\nMã có hiệu lực trong 5 phút.");

        javaMailSender.send(message);
    }
}
