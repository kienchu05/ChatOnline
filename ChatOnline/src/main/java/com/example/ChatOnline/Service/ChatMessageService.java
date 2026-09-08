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
import com.example.ChatOnline.Enum.MessageType;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional(rollbackFor = Exception.class)
    public ChatMessageResponse sendMessage(String senderId, ChatMessageRequest request) {
        // 1.Validate sender ton tai
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Validate conversation ton tai va sender la member trong conversation do
        Conversation conversation = conversationRepository.findByIdAndMember(request.conversationId(), senderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));

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
        conversation.setLastMessageContent(message.getContent() != null && !message.getContent().isEmpty() ? message.getContent() : "Đã gửi hình ảnh/video !");
        conversation.setLastMessageSenderName(message.getSender().getUsername());
        conversation.setLastMessageSenderId(message.getSender().getId());
        conversation.getConversationParticipantList().forEach(p -> {
            // Nếu là người gửi -> đã đọc. Nếu là người nhận -> chưa đọc
            p.setIsRead(p.getUser().getId().equals(senderId));
        });
        conversationRepository.save(conversation);

        // Lay danh sach participants (kphai sender)
        List<String> recipientsId = conversation.getConversationParticipantList()
                .stream().filter(participant -> !participant.getUser().getId().equals(senderId))
                .map(participant -> participant.getUser().getId()).toList();

        //Build Response cho ChatMessageResponse
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(message.getId())
                .tempId(request.tempId())
                .conversationId(message.getConversation().getId())
                .conversationAvatar(message.getConversation().getConversationAvatar())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getUsername())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .messageMedia(message.getMessageMediaList().stream()
                        .map(messageMedia -> MessageMediaResponse.builder()
                                .fileType(messageMedia.getFileType())
                                .fileName(messageMedia.getFileName())
                                .thumbnailUrl(messageMedia.getThumbnailUrl())
                                .uploadedAt(messageMedia.getUploadedAt())
                                .build())
                        .toList())
                .build();

        //Broadcast qua Websocket cho recipients
        recipientsId.forEach(recipientId -> {
            simpMessagingTemplate.convertAndSendToUser(recipientId, "/queue/messages", response);
        });
        return response;
    }

    public PageResponse<ChatMessageResponse> getMessagesByConversationId(
            String conversationId,
            int page, int size
    ) {
        //1. lay thong tin user trong securityContextHolder(luu tru thong tin authentication cua request hien tai)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        //2. lay userId
        String userId = authentication.getName();

        //3. validate conversation ton tai va userId co la member
        Conversation conversation = conversationRepository.findByIdAndMember(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_CONVERSATION_MEMBER));

        //4. Tao page va sort theo tin nhan moi nhat theo sentAt
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "sentAt"));

        Page<ChatMessage> chatMessagePage = chatMessageRepository.findByConversationId(conversationId, pageable);

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

    @Transactional
    public void deleteMessage(String messageId, String currentUserId) {
        // 1. Tìm tin nhắn cần xóa trong DB
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn"));
        if (!message.getSender().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền xóa tin nhắn này");
        }

        Conversation conversation = message.getConversation();
        chatMessageRepository.delete(message);
        chatMessageRepository.flush();

        // 2. Kiểm tra: Nếu tin nhắn vừa xóa chính là tin nhắn hiển thị ở danh sách Home
        if (conversation.getLastMessageId() != null && conversation.getLastMessageId().equals(messageId)) {

            // 3. Tìm tin nhắn cũ liền kề
            Optional<ChatMessage> newLastMessage = chatMessageRepository
                    .findTopByConversationIdOrderBySentAtDesc(conversation.getId());

            if (newLastMessage.isPresent()) {
                ChatMessage prevMsg = newLastMessage.get();
                conversation.setLastMessageId(prevMsg.getId());
                conversation.setLastMessageContent(prevMsg.getContent());
                conversation.setLastMessageTime(prevMsg.getSentAt());
            } else {
                conversation.setLastMessageId(null);
                conversation.setLastMessageContent(null);
                conversation.setLastMessageTime(null);
            }

            conversationRepository.save(conversation);
        }
    }

    public PageResponse<ChatMessageResponse> searchMessages(String conversationId, String keyword, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<ChatMessage> messages;

        if (keyword == null || keyword.isBlank()) {
            messages = chatMessageRepository.findByConversationIdOrderBySentAtDesc(conversationId, pageable);
        } else {
            messages = chatMessageRepository.searchMessages(conversationId, keyword, pageable);
        }

        List<ChatMessageResponse> responses = messages.getContent()
                .stream()
                .map(message -> ChatMessageResponse.builder()
                        .id(message.getId())
                        .senderId(message.getSender().getId())
                        .senderName(message.getSender().getUsername())
                        .conversationId(message.getConversation().getId())
                        .content(message.getContent())
                        .createdAt(message.getSentAt())
                        .build()
                ).toList();

        return PageResponse.<ChatMessageResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(messages.getTotalPages())
                .totalElements(messages.getTotalElements())
                .content(responses)
                .build();
    }

    public List<ChatMessageResponse> getConversationMedia(String conversationId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        List<ChatMessage> messages = chatMessageRepository.findMediaMessages(conversationId, List.of(MessageType.IMAGE, MessageType.VIDEO));

        List<ChatMessageResponse> responses = messages.stream()
                .map(message -> ChatMessageResponse.builder()
                        .id(message.getId())
                        .messageType(message.getMessageType())
                        .conversationId(message.getConversation().getId())
                        .senderName(message.getSender().getUsername())
                        .senderId(message.getSender().getId())
                        .messageMedia(
                                message.getMessageMediaList() == null
                                        ? List.of()
                                        : message.getMessageMediaList().stream()
                                          .map(media -> MessageMediaResponse.builder()
                                                        .fileName(media.getFileName())
                                                        .thumbnailUrl(media.getThumbnailUrl())
                                                        .fileType(media.getFileType())
                                                        .uploadedAt(message.getSentAt())
                                                        .build())
                                          .toList()
                        )
                        .build())
                .toList();

        return responses;
    }
}
