package com.example.ChatOnline.Controller;

import com.example.ChatOnline.DTO.Response.PublicKeyResponse;
import com.example.ChatOnline.DTO.Request.PublicKeyRequest;
import com.example.ChatOnline.Service.E2EEKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class E2EEKeyController {

    private final E2EEKeyService e2EEKeyService;

    @PutMapping("/api/v1/e2ee/public-key")
    public ResponseEntity<?> savePublicKey(
            Authentication authentication,
            @Valid @RequestBody PublicKeyRequest request
    ) {
        String userId = authentication.getName();
        e2EEKeyService.savePublicKey(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/e2ee/public-key/{userId}")
    public ResponseEntity<PublicKeyResponse> getPublicKey(
            @PathVariable String userId
    ) {

        return ResponseEntity.ok(
                e2EEKeyService.getPublicKey(userId)
        );
    }
}