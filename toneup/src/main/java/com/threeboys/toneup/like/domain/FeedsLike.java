package com.threeboys.toneup.like.domain;

import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedsLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Builder
    public FeedsLike(Feed feed, UserEntity user) {
        this.feed = feed;
        this.user = user;
    }
}
