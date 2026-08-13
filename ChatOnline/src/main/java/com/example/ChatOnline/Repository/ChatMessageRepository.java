package com.example.ChatOnline.Repository;

import com.example.ChatOnline.Entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    @EntityGraph(attributePaths = {"sender"})
    Page<ChatMessage> findByConversationId(String conversationId, Pageable pageable);
    //attributePaths = {"sender"}: Eager load sender (ManyToOne relationship)
    //Tránh N+1 query: Load tất cả senders trong 1 query thay vì N queries riêng lẻ
}

