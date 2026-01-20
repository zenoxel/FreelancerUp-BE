package com.FreelancerUp.feature.chat.repository;

import com.FreelancerUp.model.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByProjectId(String projectId);

    @Query(value = "SELECT * FROM conversations WHERE CAST(:userId AS TEXT) = ANY(participant_ids) " +
           "AND is_active = true ORDER BY last_message_at DESC", nativeQuery = true)
    List<Conversation> findActiveConversationsForUser(@Param("userId") UUID userId);

    @Query(value = "SELECT * FROM conversations WHERE CAST(:userId AS TEXT) = ANY(participant_ids) " +
           "AND is_active = true AND last_message_at < :since", nativeQuery = true)
    List<Conversation> findUnreadConversations(
        @Param("userId") UUID userId,
        @Param("since") LocalDateTime since
    );
}
