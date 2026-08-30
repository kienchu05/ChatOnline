package com.example.ChatOnline.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordReset {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;

    private String otp;

    private LocalDateTime expiresAt;

    private Boolean verified = false;

    private LocalDateTime createdAt;
}
