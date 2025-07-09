package com.threeboys.toneup.feed.repository;

import com.threeboys.toneup.feed.dto.FeedDetailDto;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomFeedRepository {
    List<FeedDetailDto> findFeedWithUserAndImageAndIsLiked(@Param("feedId") Long feedId, @Param("loginUserId") Long userId);

}
