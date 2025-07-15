package com.threeboys.toneup.feed.repository;

import com.threeboys.toneup.feed.dto.FeedDetailDto;
import com.threeboys.toneup.feed.dto.FeedPageItemResponse;
import com.threeboys.toneup.feed.dto.FeedPreviewResponse;
import com.threeboys.toneup.feed.dto.FeedRankingPageItemResponse;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomFeedRepository {
    List<FeedDetailDto> findFeedWithUserAndImageAndIsLiked(Long feedId, Long userId);

    FeedPageItemResponse findFeedPreviewsWithImageAndIsLiked(Long userId, Long cursor, int pageSize);

    FeedRankingPageItemResponse findRankingFeedPreviewsWithImageAndIsLiked(Long userId, Long cursor, int limit);

}
