package com.threeboys.toneup.chat.repository;

import com.threeboys.toneup.chat.domain.ChatRooms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomsRepository extends JpaRepository<ChatRooms, Long> {
}
