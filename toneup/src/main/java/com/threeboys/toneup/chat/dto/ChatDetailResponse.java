package com.threeboys.toneup.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.threeboys.toneup.chat.domain.MessageType;
import com.threeboys.toneup.user.entity.UserEntity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
        private MessageType contentType;
    }

    public static ChatDetailResponse of(UserEntity partnerUser, String profileUrl, List<MessageDetailDto> messages) {
        return ChatDetailResponse.builder()
                .partnerId(partnerUser.getId())
                .partnerNickname(partnerUser.getNickname())
                .partnerProfileImageUrl(profileUrl)
                .messages(messages)
                .build();
    }
}
