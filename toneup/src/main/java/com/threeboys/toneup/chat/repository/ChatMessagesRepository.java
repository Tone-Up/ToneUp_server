package com.threeboys.toneup.chat.repository;


import com.threeboys.toneup.chat.domain.ChatMessages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {
}
