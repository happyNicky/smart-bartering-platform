package com.finalyear.liwatch.chat.chat_managment.chatjpafiles;

import com.finalyear.liwatch.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat,Long> {
    @Query("""
        SELECT DISTINCT c
        FROM Chat c
        JOIN FETCH c.negotiation n
        JOIN FETCH n.barter b
        JOIN FETCH b.userA
        JOIN FETCH b.userB
        WHERE b.userA.id = :userId
           OR b.userB.id = :userId
    """)
    List<Chat> findChatsByUserId(@Param("userId") Long userId);
}
