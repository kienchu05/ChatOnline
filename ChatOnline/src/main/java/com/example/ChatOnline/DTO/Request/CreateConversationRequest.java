package com.example.ChatOnline.DTO.Request;

import com.example.ChatOnline.Enum.ConversationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateConversationRequest {
    String name;
    String conversationAvatar; // avatar cua GROUP

    @NotNull(message = "Conversation type is required !")
    ConversationType conversationType;

    @NotEmpty(message = "Participant ids is required !")
    List<String> participantIds;
}
