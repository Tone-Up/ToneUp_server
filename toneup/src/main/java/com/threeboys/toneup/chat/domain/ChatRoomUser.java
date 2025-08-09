package com.threeboys.toneup.chat.domain;

import com.threeboys.toneup.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @JoinColumn(name = "chatRoom_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRooms chatRoom;

    public ChatRoomUser(UserEntity user, ChatRooms chatRoom) {
        this.user = user;
        this.chatRoom = chatRoom;
    }
}
