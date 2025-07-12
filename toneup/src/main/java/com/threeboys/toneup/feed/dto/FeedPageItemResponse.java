package com.threeboys.toneup.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class FeedPageItemResponse {
    private List<FeedPreviewResponse> feeds;
    private Long nextCursor;
    private boolean hasNext;
}
