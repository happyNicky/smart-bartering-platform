package com.finalyear.liwatch.chat_managment.chatjpafiles;

import com.finalyear.liwatch.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat,Long> {
}
