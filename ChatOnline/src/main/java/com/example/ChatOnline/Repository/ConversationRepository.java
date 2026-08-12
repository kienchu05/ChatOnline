package com.example.ChatOnline.Repository;

import com.example.ChatOnline.Entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    // @EntityGraph: Eager load participants và user để tránh N+1 query problem
    // attributePaths: Specify các relationships cần load cùng lúc
    @EntityGraph(attributePaths = {"conversationParticipantList", "conversationParticipantList.user"})
    Optional<Conversation> findByParticipantHash(String participantHash);


    // Query tất cả conversations mà user tham gia
    // @EntityGraph: Eager load participants và user để tránh N+1 query
    // DISTINCT: Tránh duplicate khi JOIN với participants (1 conversation có nhiều participants)
    // ORDER BY: Sắp xếp theo lastMessageTime giảm dần, conversation có tin nhắn mới nhất lên đầu
    // NULLS LAST: Conversation chưa có tin nhắn (lastMessageTime = null) xuống cuối
    @EntityGraph(attributePaths = {"conversationParticipantList", "conversationParticipantList.user"})
    @Query("select distinct c from Conversation c JOIN c.conversationParticipantList p where p.user.id = :userId order by c.lastMessageTime desc nulls last ")
    Page<Conversation> findAllByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("select c from Conversation c where c.id = :conversationId and exists(select p from c.conversationParticipantList p where p.user.id = :userId)")
    Optional<Conversation> findByIdAndMember(String conversationId, String userId);
}
