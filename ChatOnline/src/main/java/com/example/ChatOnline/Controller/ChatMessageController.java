package com.example.ChatOnline.Controller;

import com.example.ChatOnline.DTO.Request.ChatMessageRequest;
import com.example.ChatOnline.DTO.Response.ApiResponse;
import com.example.ChatOnline.DTO.Response.ChatMessageResponse;
import com.example.ChatOnline.DTO.Response.PageResponse;
import com.example.ChatOnline.Service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @PostMapping("/api/v1/chat-messages")
    public ApiResponse<ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ChatMessageRequest request
    ){
        var sendId = jwt.getSubject();
        var data = chatMessageService.sendMessage(sendId, request);

        return ApiResponse.<ChatMessageResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Message sent successfully !")
                .data(data)
                .build();
    }
    
    @GetMapping("/api/v1/{conversationId}/messages")
    public ApiResponse<PageResponse<ChatMessageResponse>> getMessages(
            @PathVariable("conversationId") String conversationId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ){
        var data = chatMessageService.getMessagesByConversationId(conversationId, page, size);

        return ApiResponse.<PageResponse<ChatMessageResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Messages retrieved successfully !")
                .data(data)
                .build();
    }
}
