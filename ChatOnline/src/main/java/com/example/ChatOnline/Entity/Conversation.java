package com.example.ChatOnline.Entity;

import com.example.ChatOnline.Enum.ConversationType;
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
@Table(name = "conversation")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String conversationAvatar;

    private ConversationType conversationType;

    @Column(name = "participant_hash", unique = true) // Đảm bảo 2 user chỉ có 1 conversation duy nhất
    // ví dụ : User A (id=1) và User B (id=2) → hash = "1_2"
    private String participantHash; //Khi tạo conversation mới, check hash để tránh duplicate

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ConversationParticipant> conversationParticipantList = new ArrayList<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    private LocalDateTime createdAt;

    private String lastMessageId; // lưu tin nhắn cuối cùng để hiển thị phần preview

    private String lastMessageContent;

    private LocalDateTime lastMessageTime;

    //Helper method de them participants vao conversation
    public void addParticipants(User user){
        conversationParticipantList.add(ConversationParticipant.builder()
                .conversation(this)
                .user(user)
                .build());
    }
}
