package com.example.ChatOnline.Mapper;

import com.example.ChatOnline.DTO.Response.ConversationDetailResponse;
import com.example.ChatOnline.DTO.Response.CreateConversationResponse;
import com.example.ChatOnline.DTO.Response.ParticipantResponse;
import com.example.ChatOnline.Entity.Conversation;
import com.example.ChatOnline.Entity.ConversationParticipant;
import com.example.ChatOnline.Enum.ConversationType;
import com.example.ChatOnline.Service.UserSessionService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ConversationMapper {
    private final UserSessionService userSessionService;

    public CreateConversationResponse toConversationResponse(String creatorId, Conversation conversation){
        ConversationType conversationType = conversation.getConversationType();
        //Thong tin co ban ve conversation
        CreateConversationResponse response = CreateConversationResponse.builder()
                .id(conversation.getId())
                .conversationType(conversationType)
                //Map ds participants sang ParticipantResponse
                .participantInfo(conversation.getConversationParticipantList().stream()
                        .map(participants -> ParticipantResponse.builder()
                                .userId(participants.getUser().getId())
                                .username(participants.getUser().getUsername())
                                .build())
                        .toList())
                .createdAt(conversation.getCreatedAt())
                .build();

        //Xu ly ten conversation khac nhau cho PRIVATE va GROUP
        String name = resolveConversationName(creatorId, conversation);
        response.setName(name);

        // Chỉ set avatar cho GROUP conversation
        if (conversation.getConversationType() != ConversationType.PRIVATE) {
            response.setConversationAvatar(conversation.getConversationAvatar());
        }

        return response;
    }

    public ConversationDetailResponse toConversationDetailResponse(String creatorId, Conversation conversation){
        ConversationType conversationType = conversation.getConversationType();

        Boolean isRead = conversation.getConversationParticipantList().stream()
                .filter(p -> p.getUser().getId().equals(creatorId))
                .findFirst()
                .map(ConversationParticipant::getIsRead)
                .orElse(true);

        ConversationDetailResponse response = ConversationDetailResponse.builder()
                .id(conversation.getId())
                .conversationType(conversationType)
                .participantInfo(conversation.getConversationParticipantList().stream()
                        .map(participant -> ParticipantResponse.builder()
                                .userId(participant.getUser().getId())
                                .username(participant.getUser().getUsername())
                                .build())
                        .toList())
                .lastMessageTime(conversation.getLastMessageTime())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageId(conversation.getLastMessageId())
                .isRead(isRead)
                .build();

        if (conversationType == ConversationType.PRIVATE) {
            conversation.getConversationParticipantList().stream()
                    .filter(p -> !p.getUser().getId().equals(creatorId))
                    .findFirst()
                    .ifPresent(p -> {
                        String otherUserId = p.getUser().getId();
                        boolean isOnline = userSessionService.isOnline(p.getUser().getId());
                        String lastOnlineAt = userSessionService.getPresence(otherUserId)
                                .map(presence -> formatLastOnlineAt(presence.getLastOnlineAt()))
                                .orElse(null);

                        response.setIsOnline(isOnline);
                        response.setLastOnlineAt(lastOnlineAt);
                    });
        } else {
            // Group conversation: Check if any member is online
            boolean anyOnline = conversation.getConversationParticipantList().stream()
                    .filter(p -> !p.getUser().getId().equals(creatorId))
                    .anyMatch(p -> userSessionService.isOnline(p.getUser().getId()));

            response.setIsOnline(anyOnline);
        }


        //Ten cua conversation
        String name = resolveConversationName(creatorId, conversation);
        response.setName(name);

        //Chi set avatar cho GROUP conversation
        if(conversationType == ConversationType.GROUP){
            response.setConversationAvatar(conversation.getConversationAvatar());
        }
        return response;
    }

    // Helper method để resolve tên conversation
    // PRIVATE: Tên của người còn lại (không phải creatorId)
    // GROUP: Tên nhóm
    public static String resolveConversationName(String creatorId, Conversation conversation){
        if(conversation.getConversationType() == ConversationType.PRIVATE){
            return conversation.getConversationParticipantList()
                    .stream().filter(p -> !p.getUser().getId().equals(creatorId))
                    .findFirst()
                    .map(p -> p.getUser().getUsername())
                    .orElse(null);
        }
        return conversation.getName();
    }

    private String formatLastOnlineAt(Instant lastOnlineAt) {
        if (lastOnlineAt == null) return null;

        long minutes = Duration.between(lastOnlineAt, Instant.now()).toMinutes();

        if (minutes < 1)    return "Vừa hoạt động xong";
        if (minutes < 60)   return "Hoạt động " + minutes + " phút trước";
        if (minutes < 1440) return "Hoạt động " + (minutes / 60) + " giờ trước";
        return "Hoạt động " + (minutes / 1440) + " ngày trước";
    }
}
