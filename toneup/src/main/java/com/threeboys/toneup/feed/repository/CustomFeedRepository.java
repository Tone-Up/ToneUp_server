package com.threeboys.toneup.feed.repository;

import com.threeboys.toneup.feed.dto.FeedDetailDto;
import com.threeboys.toneup.feed.dto.FeedPreviewResponse;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomFeedRepository {
    List<FeedDetailDto> findFeedWithUserAndImageAndIsLiked(Long feedId, Long userId);
    List<FeedPreviewResponse> findFeedPreviewsWithImageAndIsLiked(Long feedId, Long userId, Long cursor, int pageSize);
}
