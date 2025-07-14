package com.threeboys.toneup.diary.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class DiaryPreviewResponse {
    private Long diaryId;
    private String  title;
    private String imageUrl;

    @QueryProjection
    public DiaryPreviewResponse(Long diaryId, String title, String imageUrl) {
        this.diaryId = diaryId;
        this.title = title;
        this.imageUrl = imageUrl;
    }
}
