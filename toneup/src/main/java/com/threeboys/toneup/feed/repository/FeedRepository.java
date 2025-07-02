package com.threeboys.toneup.feed.repository;

import com.threeboys.toneup.feed.domain.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
}
