package com.threeboys.toneup.chat.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class ChatRooms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long LastMessageId;
    private String LastMessageContent;

    public void updateLastMessage(Long messageId, String content) {
        this.LastMessageContent = content;
        this.LastMessageId = messageId;
    }

//    public ChatRooms(Long roomId) {
//        this.id = roomId;
//    }
}
