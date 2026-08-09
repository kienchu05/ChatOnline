package com.example.ChatOnline.Mapper;

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
        if(conversationType == ConversationType.PRIVATE){
            conversation.getConversationParticipantList()
                    .stream()
                    .filter(participant -> {
                        String participantId = String.valueOf(participant.getUser().getId());
                        String currentUserId = String.valueOf(creatorId);
                        return !participantId.equals(currentUserId);
                    })
                    .findFirst()
                    .ifPresent(participant ->
                            response.setName(
                                    participant.getUser().getUsername()
                            ));
        }
        else {
            // Với GROUP: dùng tên nhóm và avatar từ conversation
            response.setName(conversation.getName());
            response.setConversationAvatar(conversation.getConversationAvatar());
        }
        return response;
    }
}
