package com.threeboys.toneup.feed.repository;

import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.feed.dto.FeedDetailDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Query("""
    SELECT new com.threeboys.toneup.feed.dto.FeedDetailDto(
        f.id,f.content, u.id, u.nickname, pi.s3Key, fi.s3Key, CASE WHEN l.id IS NOT NULL THEN true ELSE false END )
    FROM Feed f
    JOIN f.userId u
    LEFT JOIN u.profileImageId pi
    LEFT JOIN Images fi ON fi.type = 'FEED' AND fi.refId = :feedId
    LEFT JOIN FeedsLike l ON l.feed.id = f.id AND l.user.id = :loginUserId
    WHERE f.id = :feedId
    ORDER BY fi.ImageOrder ASC
    """)
    List<FeedDetailDto> findFeedWithUserAndImageAndIsLiked(@Param("feedId") Long feedId, @Param("loginUserId") Long userId);
}
