package com.example.ChatOnline.Entity;

import com.example.ChatOnline.Enum.MessageType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING) // Lưu tên Enum vào Database
    @Column(name = "message_type", nullable = false)
    private MessageType messageType =  MessageType.TEXT;

    @Column(name = "sent_at", nullable = false)
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MessageMedia> messageMediaList = new ArrayList<>();
}
