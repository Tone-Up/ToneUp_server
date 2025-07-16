package com.threeboys.toneup.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@AllArgsConstructor
@Data
public class FeedRankingPageItemResponse{
    private List<FeedPreviewResponse> feeds;
    private Long nextCursor;
    private boolean hasNext;
}
