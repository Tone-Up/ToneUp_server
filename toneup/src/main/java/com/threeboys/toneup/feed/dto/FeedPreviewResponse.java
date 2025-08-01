package com.threeboys.toneup.feed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.Getter;

@Data
public class FeedPreviewResponse {
    private Long feedId;
    private String imageUrl;
    @JsonProperty("isLiked")
    private boolean isLiked;

    @QueryProjection
    public FeedPreviewResponse(Long feedId, String imageUrl, boolean isLiked) {
        this.feedId = feedId;
        this.imageUrl = imageUrl;
        this.isLiked = isLiked;
    }
}
