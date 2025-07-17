package com.threeboys.toneup.feed.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedPageItemResponse {
    private List<FeedPreviewResponse> feeds;
    private Long nextCursor;
    private boolean hasNext;
    private Long totalCount;
}
