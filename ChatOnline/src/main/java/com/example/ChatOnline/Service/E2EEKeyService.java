package com.example.ChatOnline.Service;

import com.example.ChatOnline.DTO.Request.PublicKeyRequest;
import com.example.ChatOnline.DTO.Response.PublicKeyResponse;
import com.example.ChatOnline.Entity.UserPublicKey;
import com.example.ChatOnline.Repository.UserPublicKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class E2EEKeyService {
    private final UserPublicKeyRepository repository;

    public void savePublicKey(String userId, PublicKeyRequest request){
        UserPublicKey publicKey = repository.findByUserId(userId)
                .orElse(
                        UserPublicKey.builder()
                                .userId(userId)
                                .algorithm(request.getAlgorithm())
                                .createdAt(LocalDateTime.now())
                                .build()
                );

        publicKey.setPublicKey(request.getPublicKey());
        publicKey.setAlgorithm(request.getAlgorithm());
        publicKey.setUpdatedAt(LocalDateTime.now());

        repository.save(publicKey);
    }

    public PublicKeyResponse getPublicKey(String userId) {

        UserPublicKey key = repository
                .findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Public key not found"));

        return PublicKeyResponse.builder()
                .userId(key.getUserId())
                .publicKey(key.getPublicKey())
                .algorithm(key.getAlgorithm())
                .build();
    }
}
