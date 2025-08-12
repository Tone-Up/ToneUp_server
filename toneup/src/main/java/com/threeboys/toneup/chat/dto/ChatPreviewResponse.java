package com.threeboys.toneup.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ChatPreviewResponse {

    private List<ChatPreviewDto> chatList;
    private Long nextCursor;
    private boolean hasNext = false;
    private Long totalCount;

    public ChatPreviewResponse(List<ChatPreviewDto> chatList) {
        this.chatList = chatList;
    }
}
