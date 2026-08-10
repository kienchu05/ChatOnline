package com.example.ChatOnline.Mapper;

import com.example.ChatOnline.DTO.Response.ConversationDetailResponse;
import com.example.ChatOnline.DTO.Response.CreateConversationResponse;
import com.example.ChatOnline.DTO.Response.ParticipantResponse;
import com.example.ChatOnline.Entity.Conversation;
import com.example.ChatOnline.Entity.ConversationParticipant;
import com.example.ChatOnline.Enum.ConversationType;

public class ConversationMapper {
    private ConversationMapper(){
    }

    public static CreateConversationResponse toConversationResponse(String creatorId, Conversation conversation){
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

    public static ConversationDetailResponse toConversationDetailResponse(String creatorId, Conversation conversation){
        ConversationType conversationType = conversation.getConversationType();

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
                .build();

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
}
