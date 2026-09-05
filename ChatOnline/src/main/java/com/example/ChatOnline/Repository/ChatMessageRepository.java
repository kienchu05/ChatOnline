package com.example.ChatOnline.Repository;

import com.example.ChatOnline.Entity.ChatMessage;
import com.example.ChatOnline.Enum.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    @EntityGraph(attributePaths = {"sender"})
    Page<ChatMessage> findByConversationId(String conversationId, Pageable pageable);
    //attributePaths = {"sender"}: Eager load sender (ManyToOne relationship)
    //Tránh N+1 query: Load tất cả senders trong 1 query thay vì N queries riêng lẻ
    Optional<ChatMessage> findTopByConversationIdOrderBySentAtDesc(String conversationId);

    @Query("""
        SELECT m
        FROM ChatMessage m
        WHERE m.conversation.id = :conversationId
        AND LOWER(m.content) LIKE
            LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY m.sentAt DESC
    """)
    Page<ChatMessage> searchMessages(@Param("conversationId") String conversationId, @Param("keyword") String keyword, Pageable pageable);

    Page<ChatMessage> findByConversationIdOrderBySentAtDesc(String conversationId,Pageable pageable);

    @Query("""
    SELECT m
    FROM ChatMessage m
    WHERE m.conversation.id = :conversationId
    AND m.messageType IN :types
    ORDER BY m.sentAt DESC
""")
    List<ChatMessage> findMediaMessages(
            @Param("conversationId") String conversationId,
            @Param("types") List<MessageType> types
    );
}

