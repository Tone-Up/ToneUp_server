package com.threeboys.toneup.like.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeedLikeResponse {
    private Long feedId;
    private boolean isLiked;
}
