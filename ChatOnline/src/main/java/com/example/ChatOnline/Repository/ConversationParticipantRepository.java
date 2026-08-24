package com.example.ChatOnline.Repository;

import com.example.ChatOnline.Entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant,String> {
    Optional<ConversationParticipant> findByConversationIdAndUserId(String conversationId, String userId);
}
