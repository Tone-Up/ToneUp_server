package com.threeboys.toneup.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WriterResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
}
