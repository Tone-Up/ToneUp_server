package com.threeboys.toneup.diary.dto;

import com.threeboys.toneup.feed.dto.FeedPreviewResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class DiaryPageItemResponse {
    private List<DiaryPreviewResponse> diaries;
    private Long nextCursor;
    private boolean hasNext;
    private Long totalCount;
}