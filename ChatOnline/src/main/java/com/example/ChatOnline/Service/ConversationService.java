package com.example.ChatOnline.Service;

import com.example.ChatOnline.DTO.Request.CreateConversationRequest;
import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.DTO.Response.ConversationDetailResponse;
import com.example.ChatOnline.DTO.Response.CreateConversationResponse;
import com.example.ChatOnline.DTO.Response.PageResponse;
import com.example.ChatOnline.Entity.Conversation;
import com.example.ChatOnline.Entity.ConversationParticipant;
import com.example.ChatOnline.Entity.User;
import com.example.ChatOnline.Enum.ConversationType;
import com.example.ChatOnline.Enum.ErrorCode;
import com.example.ChatOnline.Exception.AppException;
import com.example.ChatOnline.Mapper.ConversationMapper;
import com.example.ChatOnline.Repository.ConversationRepository;
import com.example.ChatOnline.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public CreateConversationResponse createConversation(String creatorId, CreateConversationRequest request) {
        List<String> participantIds = request.getParticipantIds();

        //dam bao creator cung nam trong nhom
        if (!participantIds.contains(creatorId)) {
            participantIds.add(creatorId);
        }

        //Lay trong tin tat ca participants tu database
        List<User> participantsInfo = userRepository.findAllById(participantIds);

        //Kiem tra xem tat ca participants co ton tai hay khong
        if (participantsInfo.size() != participantIds.size()) {
            throw new AppException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        ConversationType conversationType = request.getConversationType();
        String participantHash = null;

        //Xu li logic cho cuoc tro chuyen la PRIVATE
        if (conversationType == ConversationType.PRIVATE) {
            if (participantsInfo.size() != 2) {
                throw new AppException(ErrorCode.INVALID_PARTICIPANT_COUNT);
            }

            // Tạo participant hash để identify unique conversation
            // Sort userId để đảm bảo hash luôn giống nhau cho cùng 2 người
            // Ví dụ: userId1="abc", userId2="xyz" -> hash="abc_xyz"

            participantHash = participantsInfo.stream()
                    .map(user -> user.getId())
                    .sorted()
                    .collect(Collectors.joining("_"));

            //Kiem tra xem cuoc tro chuyen nay da ton tai hay chua
            Optional<Conversation> conversation = conversationRepository.findByParticipantHash(participantHash);
            if (conversation.isPresent()) {
                //Tra ve cuoc tro chuyen cu neu da ton tai
                return ConversationMapper.toConversationResponse(creatorId, conversation.get());
            }
        }

        //Xu li logic cho cuoc tro chuyen la GROUP
        if (conversationType == ConversationType.GROUP) {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new AppException(ErrorCode.CONVERSATION_NAME_REQUIRED);
            }
            if (participantsInfo.size() < 3) {
                throw new AppException(ErrorCode.GROUP_CONVERSATION_MINIMUM_THREE_PARTICIPANTS);
            }
        }

        //Tao convers moi
        Conversation conversation = Conversation.builder()
                .name(request.getName())
                .conversationType(conversationType)
                .conversationAvatar(request.getConversationAvatar())
                .participantHash(participantHash) // Chỉ có giá trị với PRIVATE
                .createdAt(LocalDateTime.now())
                .build();

        //Them tat ca participants vao
//            for(User user : participantsInfo){
//                conversation.addParticipants(user);
//            }
        participantsInfo.forEach(conversation::addParticipants);
        conversationRepository.save(conversation);
        //Map entity sang response DTO
        return ConversationMapper.toConversationResponse(creatorId, conversation);
    }

    public PageResponse<ConversationDetailResponse> getMyConversation(
            String userId,
            int page,
            int size){
        Pageable pageable = PageRequest.of(page - 1, size); // lay index tu chi so 0
        //Query conversations tu database voi pagination
        Page<Conversation> conversationPage = conversationRepository.findAllByUserId(userId, pageable);
        //Lay ds conversation tu page object
        List<Conversation> conversations = conversationPage.getContent();
        //Map tu entity conversations sang DTO response
        List<ConversationDetailResponse> responses = conversations.stream()
                .map(conversation -> ConversationMapper.toConversationDetailResponse(userId, conversation))
                .toList();

        //Build response voi thong tin pagination
        return PageResponse.<ConversationDetailResponse>builder()
                .currentPage(page) // Page number goc (1)
                .pageSize(pageable.getPageSize())
                .totalPages(conversationPage.getTotalPages())
                .totalElements(conversationPage.getTotalElements())
                .content(responses)
                .build();
    }
}
