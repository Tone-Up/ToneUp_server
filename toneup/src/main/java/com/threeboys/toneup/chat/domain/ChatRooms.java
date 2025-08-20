package com.threeboys.toneup.chat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class ChatRooms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    private Long LastMessageId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastSentAt;
    @Column(columnDefinition = "LONGTEXT")
    private String lastMessageContent;

    private boolean isActive = true;


    public void updateLastMessage(LocalDateTime lastSentAt, String content) {
        this.lastSentAt = lastSentAt;
        this.lastMessageContent = content;
    }

    public void changeIsActive(boolean isActive) {
        this.isActive = isActive;
    }

//    public ChatRooms(Long roomId) {
//        this.id = roomId;
//    }
}
