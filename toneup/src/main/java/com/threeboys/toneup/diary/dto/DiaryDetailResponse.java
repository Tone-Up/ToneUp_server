package com.threeboys.toneup.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.threeboys.toneup.feed.dto.FeedDetailDto;
import com.threeboys.toneup.feed.dto.FeedDetailResponse;
import com.threeboys.toneup.feed.dto.WriterResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class DiaryDetailResponse {
    private Long diaryId;
    private WriterResponse writer;
    private List<String> imageUrls;
    private String title;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static DiaryDetailResponse from(DiaryDetailDto diaryDetailDto, String profileUrl, List<String> imageUrls){
        return DiaryDetailResponse.builder()
                .diaryId(diaryDetailDto.getDiaryId())
                .writer(new WriterResponse(diaryDetailDto.getUserId(), diaryDetailDto.getNickname(), profileUrl))
                .title(diaryDetailDto.getTitle())
                .content(diaryDetailDto.getContent())
                .createdAt(LocalDateTime.now())
                .imageUrls(imageUrls)
                .build();
    }
}
