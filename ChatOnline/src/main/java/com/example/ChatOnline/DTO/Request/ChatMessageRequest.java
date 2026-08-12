package com.example.ChatOnline.DTO.Request;

import com.example.ChatOnline.Enum.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Record giong voi class nhung record su dung cho du lieu dang immutable - sau khi tao object se khong the thay doi truc tiep dc
//
public record ChatMessageRequest(
        String tempId, //map tempId voi messageId thuc ,
        // User se nhin thay tin nhan truc tiep luon ma khong can doi response
        @NotBlank(message = "ConversationId is required !")
        String coversationId,
        String content,

        @NotNull(message = "Message type is required !")
        MessageType messageType,
        List<MessageMediaRequest> messageMedia // danh sach media files (optional)
) {
}
