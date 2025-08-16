package com.threeboys.toneup.chat.repository;

import com.threeboys.toneup.chat.domain.ChatRooms;
import com.threeboys.toneup.chat.dto.ChatListRequest;
import com.threeboys.toneup.chat.dto.ChatPreviewDto;
import com.threeboys.toneup.chat.dto.ChatPreviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ChatRoomsRepository extends JpaRepository<ChatRooms, Long> {

    @Query("""
    SELECT new com.threeboys.toneup.chat.dto.ChatPreviewDto(
        r.id,
        u.id,
        u.nickname,
        i.s3Key,
        r.lastMessageContent,
        r.lastSentAt,
        COUNT(m)
    )
    FROM ChatRoomUser c
    JOIN c.chatRoom r
    JOIN ChatRoomUser c2 ON c2.chatRoom = c.chatRoom AND c2.user.id <> :userId
    JOIN c2.user u
    JOIN u.profileImageId i
    LEFT JOIN ChatMessages m ON m.room = r AND m.senderId <> :userId AND m.unreadCount >= 1
    WHERE c.user.id = :userId
    GROUP BY r.id, u.id, u.nickname, i.s3Key, r.lastMessageContent, r.lastSentAt
    ORDER BY r.id DESC
    """)

    Page<ChatPreviewDto> findUserIdChatList(@Param("userId") Long userId, Pageable pageable);

    @Query("select r from ChatRooms r, ChatMessages m where r.id = m.room.id and r.isActive = false and r.id in :userRoomIds and m.type = 'LEAVE' and m.senderId =:peerId order by m.sentAt desc")
    List<ChatRooms> findByIsActiveFalseAndRoomsId(@Param("userRoomIds")Set<Long> userRoomIds, @Param("peerId")Long peerId, Pageable pageable);
}


