package com.threeboys.toneup.feed.dto;

import java.util.List;

public class FeedPageItemResponse {
    private List<FeedPreviewResponse> feeds;
    private int nextCursor;
    private boolean hasNext;
}
