package com.threeboys.toneup.feed.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FeedDetailResponse {
    private Long feedId;
    private WriterResponse writer;
    private List<String> imageUrls;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private int likeCount;
    private boolean isLiked;

    public static FeedDetailResponse from(FeedDetailDto feedDetailDto, String profileUrl, List<String> imageUrls){
        return FeedDetailResponse.builder()
                .feedId(feedDetailDto.getFeedId())
                .writer(new WriterResponse(feedDetailDto.getUserId(), feedDetailDto.getNickname(), profileUrl))
                .content(feedDetailDto.getContent())
                .createdAt(LocalDateTime.now())
                .likeCount(feedDetailDto.getLikeCount())
                .isLiked(feedDetailDto.isLiked())
                .imageUrls(imageUrls)
                .build();
    }
}
