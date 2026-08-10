package com.example.ChatOnline.Controller;

import com.example.ChatOnline.DTO.Request.CreateConversationRequest;
import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.DTO.Response.CreateConversationResponse;
import com.example.ChatOnline.Entity.Conversation;
import com.example.ChatOnline.Service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @PostMapping("/api/v1/conversations")
    ApiResponse<CreateConversationResponse> createConversation(
            @AuthenticationPrincipal Jwt jwt, // lay tt nguoi dung tu token
            @RequestBody @Valid CreateConversationRequest request
            ){
        //Lay userId tu JWT sau khi da generate token
        var creatorId = jwt.getSubject();

        //Goi service de tao conversation
        var data = conversationService.createConversation(creatorId,request);

        return ApiResponse.<CreateConversationResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Conversation created successfully")
                .data(data)
                .build();
    }

    @GetMapping("/api/v1/my-conversations")
    public ApiResponse<PageResponse<ConversationDetailResponse>> getMyConversation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ){
        var userId = jwt.getSubject();
        var data = conversationService.getMyConversation(userId, page, size);

        return ApiResponse.<PageResponse<ConversationDetailResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Conversation retrieved successfully !")
                .data(data)
                .build();
    }
}
