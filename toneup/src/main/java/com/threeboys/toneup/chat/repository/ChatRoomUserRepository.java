package com.threeboys.toneup.chat.repository;

import com.threeboys.toneup.chat.domain.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    List<ChatRoomUser> findByChatRoomId(Long roomId);

    void deleteByChatRoomIdAndUserId(Long chatRoomId, Long userId);

    List<ChatRoomUser> findByUserId(Long userId);
}