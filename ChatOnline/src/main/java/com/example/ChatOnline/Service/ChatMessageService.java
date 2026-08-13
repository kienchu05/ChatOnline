package com.example.ChatOnline.Service;

import com.example.ChatOnline.DTO.Request.ChatMessageRequest;
import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.DTO.Response.ChatMessageResponse;
import com.example.ChatOnline.DTO.Response.MessageMediaResponse;
import com.example.ChatOnline.DTO.Response.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Conversation conversation = conversationRepository.findByIdAndMember(request.conversationId(), senderId)
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

        // neu gui media ma khong luu message_id thi se bi loi nullpointer
        //neu k gui media thi List<MessageMedia> media se null , khi do khong cascade sang entity MessageMedia nen se k can luu message_id
        media.forEach(m -> m.setMessage(message));

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

    public PageResponse<ChatMessageResponse> getMessagesByConversationId(
            String conversationId,
            int page, int size
    ){
        //1. lay thong tin user trong securityContextHolder(luu tru thong tin authentication cua request hien tai)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        //2. lay userId
        String userId = authentication.getName();

        //3. validate conversation ton tai va userId co la member
        Conversation conversation = conversationRepository.findByIdAndMember(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));

        //4. Tao page va sort theo tin nhan moi nhat theo sentAt
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "sentAt"));

        Page<ChatMessage> chatMessagePage= chatMessageRepository.findByConversationId(conversationId, pageable);

        //5.Lay danh sach messages tu Page object
        List<ChatMessage> messages = chatMessagePage.getContent();

        //6.Map entity message sang DTO
        List<ChatMessageResponse> responses = messages.stream()
                .map(message -> ChatMessageResponse.builder()
                        .id(message.getId())
                        .conversationId(conversation.getId())
                        .conversationAvatar(conversation.getConversationAvatar())
                        .senderId(message.getSender().getId())
                        .senderName((message.getSender().getUsername()))
                        .content(message.getContent())
                        .messageType(message.getMessageType())
                        // Map media files
                        .messageMedia(message.getMessageMediaList().stream()
                                .map(messageMedia -> MessageMediaResponse.builder()
                                        .fileName(messageMedia.getFileName())
                                        .fileType(messageMedia.getFileType())
                                        .thumbnailUrl(messageMedia.getThumbnailUrl())
                                        .uploadedAt(messageMedia.getUploadedAt())
                                        .build())
                                .toList())
                        .createdAt(message.getSentAt())
                        .build())
                .toList();

        //7.return ve thong tin doan chat co pagination
        return PageResponse.<ChatMessageResponse>builder()
                .currentPage(page)
                .pageSize(pageable.getPageSize())
                .totalPages(chatMessagePage.getTotalPages())
                .totalElements(chatMessagePage.getTotalElements())
                .content(responses)
                .build();
    }
}
