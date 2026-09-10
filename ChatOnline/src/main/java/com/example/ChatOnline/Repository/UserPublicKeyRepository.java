package com.example.ChatOnline.Repository;

import com.example.ChatOnline.Entity.UserPublicKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPublicKeyRepository extends JpaRepository<UserPublicKey, Long> {

    Optional<UserPublicKey> findByUserId(String userId);
}