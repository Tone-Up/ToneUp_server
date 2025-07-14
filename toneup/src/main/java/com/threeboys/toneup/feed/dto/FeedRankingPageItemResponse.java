package com.threeboys.toneup.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@AllArgsConstructor
@Data
public class FeedRankingPageItemResponse{
    private List<FeedPreviewResponse> feeds;
    private String nextCursor;
    private boolean hasNext;
}
