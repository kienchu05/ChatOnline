package com.example.ChatOnline.Repository;

import com.example.ChatOnline.Entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant,String> {
    @Query("select c from ConversationParticipant c where c.id = :conversationId and c.user.id = :userId")
    Optional<ConversationParticipant> findByConversationIdAndUserId(String conversationId, String userId);
}
