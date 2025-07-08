package com.threeboys.toneup.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeedDetailDto {
    private Long feedId;
    private String content;
    private Long userId;
    private String nickname;
    private String profileS3Key;
    private String feedImageS3Key;
    private boolean isLiked;
}
