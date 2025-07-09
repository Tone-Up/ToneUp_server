package com.threeboys.toneup.feed.dto;

import com.querydsl.core.annotations.QueryProjection;

public class FeedPreviewResponse {
    private Long feedId;
    private String imageUrl;
    private boolean isLiked;

    @QueryProjection
    public FeedPreviewResponse(Long feedId, String imageUrl, boolean isLiked) {
        this.feedId = feedId;
        this.imageUrl = imageUrl;
        this.isLiked = isLiked;
    }
}
