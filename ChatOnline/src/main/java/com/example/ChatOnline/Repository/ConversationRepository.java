package com.example.ChatOnline.Repository;

import com.example.ChatOnline.Entity.Conversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    // @EntityGraph: Eager load participants và user để tránh N+1 query problem
    // attributePaths: Specify các relationships cần load cùng lúc
    @EntityGraph(attributePaths = {"conversationParticipantList", "conversationParticipantList.user"})
    Optional<Conversation> findByParticipantHash(String participantHash);
}
