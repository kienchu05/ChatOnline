package com.example.ChatOnline.Repository;

import com.example.ChatOnline.Entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findTopByUserIdOrderByCreatedAtDesc(String userId);
    void deleteByUserId(String userId);
}
