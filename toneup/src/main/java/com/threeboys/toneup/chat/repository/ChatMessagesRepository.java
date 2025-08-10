package com.threeboys.toneup.chat.repository;


import com.threeboys.toneup.chat.domain.ChatMessages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {
    List<ChatMessages> findByRoomIdAndIdGreaterThan(Long roomId, Long lastMessageId);
}
