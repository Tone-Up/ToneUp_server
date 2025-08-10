package com.threeboys.toneup.chat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatListRequest {
    private List<ChatStatus> chatStatusList;

    @Data
    @NoArgsConstructor
    public static class ChatStatus{
        private Long roomId;
        private Long lastMessageId;
    }
}
