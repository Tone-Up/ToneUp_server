package com.threeboys.toneup.chat.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ChatRooms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long LastMessageId;
    private String LastMessageContent;

//    public ChatRooms(Long roomId) {
//        this.id = roomId;
//    }
}
