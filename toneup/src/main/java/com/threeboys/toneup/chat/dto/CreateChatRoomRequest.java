package com.threeboys.toneup.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateChatRoomRequest {
    private Long userId;
    private Long peerId;
}
