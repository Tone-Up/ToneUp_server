package com.threeboys.toneup.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class ChatPreviewDto {
    private Long chatRoomId;
    private Long partnerId;
    private String partnerNickname;
    private String partnerProfileImageUrl;
    private String lastMessage;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastSentAt;
    private Long unreadCount;

    public ChatPreviewDto(Long chatRoomId, Long partnerId, String partnerNickname, String partnerProfileImageUrl, String lastMessage, LocalDateTime lastSentAt, Long unreadCount) {
        this.chatRoomId = chatRoomId;
        this.partnerId = partnerId;
        this.partnerNickname = partnerNickname;
        this.partnerProfileImageUrl = partnerProfileImageUrl;
        this.lastMessage = lastMessage;
        this.lastSentAt = lastSentAt;
        this.unreadCount = unreadCount;
    }
}


