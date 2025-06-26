package com.threeboys.toneup.chat.domain;

import com.threeboys.toneup.user.entity.UserEntity;
import jakarta.persistence.*;

@Entity
public class ChatRoomUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity userId;

    @JoinColumn(name = "chatRoom_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRooms chatRoomId;

}
