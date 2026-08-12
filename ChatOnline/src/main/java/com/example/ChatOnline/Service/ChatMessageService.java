package com.example.ChatOnline.Service;

import com.example.ChatOnline.DTO.Request.ChatMessageRequest;
import com.example.ChatOnline.DTO.Response.ChatMessageResponse;
import com.example.ChatOnline.DTO.Response.MessageMediaResponse;
import com.example.ChatOnline.Entity.ChatMessage;
import com.example.ChatOnline.Entity.Conversation;
import com.example.ChatOnline.Entity.MessageMedia;
import com.example.ChatOnline.Entity.User;
import com.example.ChatOnline.Enum.ErrorCode;
import com.example.ChatOnline.Exception.AppException;
import com.example.ChatOnline.Repository.ChatMessageRepository;
import com.example.ChatOnline.Repository.ConversationRepository;
import com.example.ChatOnline.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(rollbackFor = Exception.class)
    public ChatMessageResponse sendMessage(String senderId, ChatMessageRequest request){
        // 1.Validate sender ton tai
        User sender = userRepository.findById(senderId)
                .orElseThrow(() ->  new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Validate conversation ton tai va sender la member trong conversation do
        Conversation conversation = conversationRepository.findByIdAndMember(request.coversationId(), senderId)
                .orElseThrow(() ->  new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));

        // 3. Tao danh sach media files (neu co)
        List<MessageMedia> media = request.messageMedia() != null && !request.messageMedia().isEmpty() ?
                request.messageMedia().stream()
                .map(messageMedia -> MessageMedia.builder()
                                     .fileName(messageMedia.fileName())
                                     .fileType(messageMedia.fileType())
                                     .thumbnailUrl(messageMedia.thumbnailUrl())
                                     .build())
                .toList() : List.of();

        // 4.Tao chat message entity
        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.content())
                .messageType(request.messageType())
                .messageMediaList(media) // dc luu duoi dang JSON
                .build();

        chatMessageRepository.save(message);

        // 5. Update lastMessage cua conversation
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageTime(message.getSentAt());
        conversation.setLastMessageContent(message.getContent());
        conversationRepository.save(conversation);

        // 6.Map entity sang DTO
        return ChatMessageResponse.builder()
                .id(message.getId())
                .tempId(request.tempId())
                .conversationId(message.getConversation().getId())
                .conversationAvatar(message.getConversation().getConversationAvatar())
                .senderId(sender.getId())
                .senderName(sender.getUsername())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .messageMedia(message.getMessageMediaList().stream()
                        .map(messageMedia -> MessageMediaResponse.builder()
                                .fileName(messageMedia.getFileName())
                                .fileType(messageMedia.getFileType())
                                .thumbnailUrl(messageMedia.getThumbnailUrl())
                                .uploadedAt(messageMedia.getUploadedAt())
                                .build())
                        .toList())
                .createdAt(message.getSentAt())
                .build();
    }
}
