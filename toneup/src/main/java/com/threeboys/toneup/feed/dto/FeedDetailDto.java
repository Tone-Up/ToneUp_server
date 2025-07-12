package com.threeboys.toneup.feed.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class FeedDetailDto {
    private Long feedId;
    private String content;
    private Long userId;
    private String nickname;
    private String profileS3Key;
    private String feedImageS3Key;
    private int likeCount;
    private boolean isLiked;

    @QueryProjection
    public FeedDetailDto(Long feedId, String content, Long userId, String nickname, String profileS3Key, String feedImageS3Key,int likeCount, boolean isLiked) {
        this.feedId = feedId;
        this.content = content;
        this.userId = userId;
        this.nickname = nickname;
        this.profileS3Key = profileS3Key;
        this.feedImageS3Key = feedImageS3Key;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }
}
