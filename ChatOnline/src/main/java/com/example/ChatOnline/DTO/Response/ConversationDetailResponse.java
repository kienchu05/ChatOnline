package com.example.ChatOnline.DTO.Response;

import com.example.ChatOnline.Enum.ConversationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Khong serializable (Chuyen doi object -> Json) voi cac fields null
public class ConversationDetailResponse implements Serializable {
    private String id;
    private String name;
    private ConversationType conversationType;
    private String conversationAvatar;
    private List<ParticipantResponse> paticipants;

    //Thong tin tin nhan cuoi cung
    private String lastMessageId;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt;
}
