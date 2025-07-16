package com.threeboys.toneup.feed.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.QImages;
import com.threeboys.toneup.feed.domain.QFeed;
import com.threeboys.toneup.feed.dto.*;
import com.threeboys.toneup.feedLike.domain.QFeedsLike;
import com.threeboys.toneup.user.entity.QUserEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomFeedRepositoryImpl implements CustomFeedRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private static Long CUSTOM_CURSOR = 10000000L;
    public CustomFeedRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<FeedDetailDto> findFeedWithUserAndImageAndIsLiked(Long feedId, Long userId) {
        QFeed f = QFeed.feed;
        QUserEntity u = QUserEntity.userEntity;
        QImages pi = QImages.images;
        QImages fi = new QImages("fi"); // 같은 테이블 두 번 조인
        QFeedsLike l = QFeedsLike.feedsLike;
        QFeedsLike totalLikes = new QFeedsLike("totalLikes");

//        QFeedsLike userId
//            @Query("""
//        SELECT new com.threeboys.toneup.feed.dto.FeedDetailDto(
//            f.id,f.content, u.id, u.nickname, pi.s3Key, fi.s3Key, CASE WHEN l.id IS NOT NULL THEN true ELSE false END )
//        FROM Feed f
//        JOIN f.userId u
//        LEFT JOIN u.profileImageId pi
//        LEFT JOIN Images fi ON fi.type = 'FEED' AND fi.refId = :feedId
//        LEFT JOIN FeedsLike l ON l.feed.id = f.id AND l.user.id = :loginUserId
//        WHERE f.id = :feedId
//        ORDER BY fi.ImageOrder ASC
//        """)
        return jpaQueryFactory
                .select(new QFeedDetailDto(
                        f.id,
                        f.content,
                        u.id,
                        u.nickname,
                        pi.s3Key,
                        fi.s3Key,
                        totalLikes.id.count().intValue(),
                        l.id.isNotNull(),
                        f.userId.id.eq(userId)
                )).from(f)
                .join(f.userId, u)
                .leftJoin(u.profileImageId, pi)
                .leftJoin(fi).on(fi.type.eq(ImageType.FEED).and(fi.refId.eq(feedId)))
                .leftJoin(l).on(l.feed.id.eq(f.id).and(l.user.id.eq(userId)))
                .leftJoin(totalLikes).on(totalLikes.feed.id.eq(f.id))
                .where(f.id.eq(feedId))
                .groupBy(
                        f.id, f.content, u.id, u.nickname,
                        pi.s3Key, fi.s3Key, l.id
                )
                .orderBy(fi.ImageOrder.asc())
                .fetch();
    }
    public FeedPageItemResponse findFeedPreviewsWithImageAndIsLiked(Long userId, Long cursor, boolean isMine, int limit){
        QFeed feed = QFeed.feed;
        QImages image = QImages.images;
        QFeedsLike like = QFeedsLike.feedsLike;

        List<FeedPreviewResponse> feedPreviewResponseList = jpaQueryFactory.select(
                new QFeedPreviewResponse(
                        feed.id,
                        image.s3Key,
                        like.id.isNotNull()))
                .from(feed)
                .leftJoin(image)
                .on(image.refId.eq(feed.id)
                        .and(image.type.eq(ImageType.FEED))
                        .and(image.ImageOrder.eq(0)))
                .leftJoin(like)
                .on(like.feed.id.eq(feed.id)
                        .and(like.user.id.eq(userId)))
                .where(
                        cursor == null ? null : feed.id.lt(cursor),
                        (isMine ? feed.userId.id.eq(userId) : null)
                )
                .orderBy(feed.id.desc())
                .limit(limit)
                .fetch();
        boolean hasNext = feedPreviewResponseList.size() > limit;
        Long nextCursor = (feedPreviewResponseList.getLast() == null ) ? null : feedPreviewResponseList.getLast().getFeedId();

        return new FeedPageItemResponse(feedPreviewResponseList, nextCursor, hasNext) ;
    }


    public FeedRankingPageItemResponse findRankingFeedPreviewsWithImageAndIsLiked(Long userId, Long cursor,  int limit){
        QFeed feed = QFeed.feed;
        QImages image = QImages.images;
        QFeedsLike like = QFeedsLike.feedsLike;
        Integer cursorLikeCount =null;
        Long cursorId = null;

        if(cursor != null){
            cursorLikeCount = Math.toIntExact(cursor / CUSTOM_CURSOR);
            cursorId = cursor%CUSTOM_CURSOR;
        }

        BooleanBuilder cursorCondition = new BooleanBuilder();
        if (cursorLikeCount != null && cursorId != null) {
            cursorCondition.or(feed.likeCount.lt(cursorLikeCount));
            cursorCondition.or(
                    feed.likeCount.eq(cursorLikeCount)
                            .and(feed.id.lt(cursorId))
            );
        }
        List<FeedPreviewResponse> feedPreviewResponseList = jpaQueryFactory.select(
                        new QFeedPreviewResponse(
                                feed.id,
                                image.s3Key,
                                like.id.isNotNull()))
                .from(feed)
                .leftJoin(image)
                .on(image.refId.eq(feed.id)
                        .and(image.type.eq(ImageType.FEED))
                        .and(image.ImageOrder.eq(0)))
                .leftJoin(like)
                .on(like.feed.id.eq(feed.id)
                        .and(like.user.id.eq(userId)))
                .where(cursorCondition)
                .orderBy(feed.likeCount.desc(), feed.id.desc())
                .limit(limit)
                .fetch();
        boolean hasNext = feedPreviewResponseList.size() > limit;

        Long nextCursorId = (feedPreviewResponseList.getLast()==null) ? null : feedPreviewResponseList.getLast().getFeedId();
        List<Integer> nextCursorCount = (nextCursorId==null) ? null : jpaQueryFactory.select(feed.likeCount).from(feed).where(feed.id.eq(nextCursorId)).fetch();

        Long nextCursor = (nextCursorId==null) ? null : nextCursorCount.getFirst()*CUSTOM_CURSOR + nextCursorId;

        return new FeedRankingPageItemResponse(feedPreviewResponseList, nextCursor, hasNext) ;
    }
}
