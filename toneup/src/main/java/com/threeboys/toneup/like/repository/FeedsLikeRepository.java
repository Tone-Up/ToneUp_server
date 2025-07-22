package com.threeboys.toneup.like.repository;

import com.threeboys.toneup.like.domain.FeedsLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedsLikeRepository extends JpaRepository<FeedsLike, Long> {

    boolean existsByFeedIdAndUserId(Long feedId, Long userId);

    void deleteByFeedIdAndUserId(Long feedId, Long userId);

    void deleteAllByFeedId(Long testFeedId);
}
