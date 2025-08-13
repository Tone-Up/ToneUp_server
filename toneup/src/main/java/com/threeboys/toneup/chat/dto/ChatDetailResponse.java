package com.threeboys.toneup.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ChatDetailResponse {

    private Long partnerId;
    private String partnerNickname;
    private String partnerProfileImageUrl;
    private List<MessageDetailDto> messages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDetailDto {
        private Long messageId;
        private Long senderId;
        private String content;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime sentAt;
        @JsonProperty("isRead")
        private boolean isRead;
        @JsonProperty("isMine")
        private boolean isMine;
    }
}
