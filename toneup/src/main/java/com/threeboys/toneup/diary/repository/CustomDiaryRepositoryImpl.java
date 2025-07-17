package com.threeboys.toneup.diary.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.QImages;
import com.threeboys.toneup.diary.domain.QDiary;
import com.threeboys.toneup.diary.dto.*;
import com.threeboys.toneup.user.entity.QUserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomDiaryRepositoryImpl implements CustomDiaryRepository{
    private final JPAQueryFactory jpaQueryFactory;

    public List<DiaryDetailDto> findDiaryWithUserAndImage(Long diaryId, Long userId){
        QDiary d = QDiary.diary;
        QUserEntity u = QUserEntity.userEntity;
        QImages pi = QImages.images;
        QImages di = new QImages("di"); // 같은 테이블 두 번 조인

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
                .select(new QDiaryDetailDto(
                        d.id,
                        d.title,
                        d.content,
                        u.id,
                        u.nickname,
                        pi.s3Key,
                        di.s3Key
                )).from(d)
                .join(d.userId, u)
                .leftJoin(u.profileImageId, pi)
                .leftJoin(di).on(di.type.eq(ImageType.DIARY).and(di.refId.eq(diaryId)))
                .where(d.id.eq(diaryId))
                .orderBy(di.ImageOrder.asc())
                .fetch();
    }

    @Override
    public DiaryPageItemResponse findDiaryPreviewsWithImage(Long userId, Long cursor, int limit) {
        QDiary d = QDiary.diary;
        QImages i = QImages.images;

        List<DiaryPreviewResponse> diaryPreviewResponseList = jpaQueryFactory.select(
                        new QDiaryPreviewResponse(
                                d.id,
                                d.title,
                                i.s3Key))
                .from(d)
                .leftJoin(i)
                .on(i.refId.eq(d.id)
                        .and(i.type.eq(ImageType.DIARY))
                        .and(i.ImageOrder.eq(0)))
                .where(
                        cursor == null ? null : d.id.lt(cursor)
//                        ,(isMine? d.userId.id.eq(userId) : null)
                )
                .orderBy(d.id.desc())
                .limit(limit)
                .fetch();
        boolean hasNext = diaryPreviewResponseList.size() > limit;
        Long nextCursor = diaryPreviewResponseList.getLast().getDiaryId();
        Long totalCount = Optional.ofNullable(
                jpaQueryFactory
                        .select(d.count())
                        .from(d)
                        .where(d.userId.id.eq(userId))
                        .fetchOne()
        ).orElse(0L);
        return new DiaryPageItemResponse(diaryPreviewResponseList, nextCursor, hasNext, totalCount);
    }



}
