package com.threeboys.toneup.feed.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.QImages;
import com.threeboys.toneup.feed.domain.QFeed;
import com.threeboys.toneup.feed.dto.*;
import com.threeboys.toneup.like.domain.QFeedsLike;
import com.threeboys.toneup.user.entity.QUserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
                        f.user.id.eq(userId)
                )).from(f)
                .join(f.user, u)
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
    public FeedPageItemResponse findFeedPreviewsWithImageAndIsLiked(Long userId, Long cursor, boolean isMine, int limit, boolean myLike){
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
                        (isMine ? feed.user.id.eq(userId) : null),
                        (myLike ? like.user.id.eq(userId) : null)
                )
                .orderBy(feed.id.desc())
                .limit(limit+1)
                .fetch();
        boolean hasNext = feedPreviewResponseList.size()>limit;
        Long nextCursor = (hasNext) ? feedPreviewResponseList.get(limit-1).getFeedId() : null;
        Long totalCount = null;


        //사용자 좋아요 피드 페이지네이션
        if(myLike){
            totalCount = Optional.ofNullable(
                    jpaQueryFactory.select(like.count())
                            .from(like)
                            .where(like.user.id.eq(userId))
                            .fetchOne()
            ).orElse(0L);

        }else{
            //사용자 피드 페이지네이션
            if(isMine){
                totalCount = Optional.ofNullable(
                        jpaQueryFactory
                                .select(feed.count())
                                .from(feed)
                                .where(feed.user.id.eq(userId))
                                .fetchOne()
                ).orElse(0L);
            }
        }


        List<FeedPreviewResponse> feeds = (hasNext) ? feedPreviewResponseList.subList(0,feedPreviewResponseList.size()-1) : feedPreviewResponseList.subList(0,feedPreviewResponseList.size());
        return new FeedPageItemResponse(feeds, nextCursor, hasNext, totalCount) ;
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
                .limit(limit+1)
                .fetch();
        boolean hasNext = feedPreviewResponseList.size() > limit;

        Long nextCursorId = (hasNext) ? feedPreviewResponseList.get(limit-1).getFeedId() : null;
        List<Integer> nextCursorCount = (nextCursorId==null) ? null : jpaQueryFactory.select(feed.likeCount).from(feed).where(feed.id.eq(nextCursorId)).fetch();

        Long nextCursor = (nextCursorId==null) ? null : nextCursorCount.getFirst()*CUSTOM_CURSOR + nextCursorId;
        List<FeedPreviewResponse> feeds = (hasNext) ? feedPreviewResponseList.subList(0,feedPreviewResponseList.size()-1) : feedPreviewResponseList.subList(0,feedPreviewResponseList.size());

        return new FeedRankingPageItemResponse(feeds, nextCursor, hasNext) ;
    }
}
