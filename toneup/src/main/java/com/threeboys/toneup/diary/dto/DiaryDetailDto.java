package com.threeboys.toneup.diary.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class DiaryDetailDto {
    private Long diaryId;
    private String title;
    private String content;
    private Long userId;
    private String nickname;
    private String profileS3Key;
    private String diaryImageS3Key;

    @QueryProjection
    public DiaryDetailDto(Long diaryId, String title, String content, Long userId, String nickname, String profileS3Key, String diaryImageS3Key) {
        this.diaryId = diaryId;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.nickname = nickname;
        this.profileS3Key = profileS3Key;
        this.diaryImageS3Key = diaryImageS3Key;
    }
}
