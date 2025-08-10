package com.threeboys.toneup.chat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ChatPreviewResponse {
    private List<ChatPreviewList> chatList;

    @NoArgsConstructor
    @Data
    public static class ChatPreviewList {
        private Long chatRoomId;
        private Long partnerId;
        private String partnerNickname;
        private String partnerProfileImageUrl;
        private String lastMessage;
        private String lastMessageTime;
        private int unreadCount;
    }
}

