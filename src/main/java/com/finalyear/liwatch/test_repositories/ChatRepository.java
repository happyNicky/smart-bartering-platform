package com.finalyear.liwatch.test_repositories;

import com.finalyear.liwatch.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat,Long> {
    @Query("""
    SELECT c FROM Chat c
    JOIN FETCH c.sender
    WHERE c.negotiation.id = :negId
    ORDER BY c.sentAt
    """)
    List<Chat> findByNegotiation(Long negId);
}
