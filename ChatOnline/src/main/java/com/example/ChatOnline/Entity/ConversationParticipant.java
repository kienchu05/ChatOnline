package com.example.ChatOnline.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
// Bảng trung gian có qhe manytomany giữa User vs Conversation
@Table(name = "conversation_participant",
    uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "user_id"}))
            //@UniqueConstraint:
            // Đảm bảo 1 user không thể join 1 conversation 2 lần
public class ConversationParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Builder.Default
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt =  LocalDateTime.now();
}
