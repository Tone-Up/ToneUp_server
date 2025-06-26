package com.threeboys.toneup.chat.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRooms roomId;

    private Long senderId;

    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @ColumnDefault("0")
    private int unreadCount;
    public void updateUnreadCnt(int count){
        this.unreadCount = count;
    }

}
