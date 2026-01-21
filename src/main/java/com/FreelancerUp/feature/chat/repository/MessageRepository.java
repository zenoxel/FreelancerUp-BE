package com.FreelancerUp.feature.chat.repository;

import com.FreelancerUp.model.document.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(
        String conversationId,
        Pageable pageable
    );

    List<Message> findByFromUserIdOrToUserId(String fromUserId, String toUserId);

    @Query("{'conversationId': ?0, 'isRead': false, 'toUserId': ?1}")
    List<Message> findUnreadMessages(String conversationId, String toUserId);

    @Query("{$and: [" +
           "{'toUserId': ?0}, " +
           "{'isRead': false}" +
           "]}")
    List<Message> findByConversationIdAndToUserIdAndIsReadOrderByCreatedAtDesc(
            String conversationId,
            String toUserId,
            boolean isRead
    );

    @Query("{$and: [" +
           "{'toUserId': ?0}, " +
           "{'isRead': false}" +
           "]}")
    Long countByConversationIdAndToUserIdAndIsRead(String conversationId, String toUserId, boolean isRead);

    @Query("{'conversationId': ?0, 'createdAt': {$gte: ?1}}")
    List<Message> findMessagesSince(String conversationId, LocalDateTime since);
}
