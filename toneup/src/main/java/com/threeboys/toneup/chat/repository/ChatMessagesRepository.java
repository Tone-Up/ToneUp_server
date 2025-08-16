package com.threeboys.toneup.chat.repository;


import com.threeboys.toneup.chat.domain.ChatMessages;
import com.threeboys.toneup.chat.domain.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {

    List<ChatMessages> findByRoomIdAndSentAtGreaterThan(Long chatRoomId, LocalDateTime lastSentAt);


//    ChatMessages findFirstByRoomIdOrderBySentAtDesc(Long roomId);

    ChatMessages findFirstByRoomIdAndTypeOrderBySentAtDesc(Long roomId, MessageType messageType);
}
