package com.threeboys.toneup.feed.service;

import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.feed.dto.*;
import com.threeboys.toneup.feed.exception.FeedNotFoundException;
import com.threeboys.toneup.feed.repository.FeedRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedService {
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService;

    @Transactional
    public FeedResponse createFeed(Long userId, FeedRequest feedRequest){
        UserEntity user = userRepository.getReferenceById(userId);
        String content = feedRequest.getContent();
        List<String> imageUrls = feedRequest.getImageUrls();

        Feed feed  = new Feed(user, content);

        //save해서 feed id 받아오기(mysql)
        feedRepository.save(feed);
        feed.attachImages(imageUrls);
        imageRepository.saveAll(feed.getImageUrlList());

        return new FeedResponse(feed.getId());
    }
    @Transactional
    public FeedResponse updateFeed(Long userId, Long feedId,  FeedRequest feedRequest) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(FeedNotFoundException::new);
        //작성자인지 검증
        feed.validateOwner(userId);

        //s3 기존 이미지 삭제 + images 테이블 삭제
        List<Images> imagesList = imageRepository.findByTypeAndRefId(ImageType.FEED, feedId);
        imagesList.forEach(images -> {fileService.deleteS3Object(images.getS3Key());});
        imageRepository.deleteAll(imagesList);



        feed.changeFeed(feedRequest.getContent(), feedRequest.getImageUrls());
        imageRepository.saveAll(feed.getImageUrlList());

        return new FeedResponse(feedId);
    }

    public void deleteFeed(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(FeedNotFoundException::new);
        feed.validateOwner(userId);

        //s3 기존 이미지 삭제 + images 테이블 삭제
        List<Images> imagesList = imageRepository.findByTypeAndRefId(ImageType.FEED, feedId);
        imagesList.forEach(images -> {fileService.deleteS3Object(images.getS3Key());});
        imageRepository.deleteAll(imagesList);

        feedRepository.delete(feed);
    }

    public FeedDetailResponse getFeed(Long userId , Long feedId) {
        //다중 조인으로 전체 조회(프로필, 피드 ,이미지들, 좋아요여부)
        List<FeedDetailDto> feedDetailDtoList = feedRepository.findFeedWithUserAndImageAndIsLiked(feedId, userId);
        if(feedDetailDtoList.isEmpty()) throw new FeedNotFoundException();
        // 이미지 s3Key로 s3 조회해서 url 획득 + 프로필 이미지도 획득
        List<String> imageUrls = feedDetailDtoList.stream()
                .map(feedDetailDto -> fileService.getPreSignedUrl(feedDetailDto.getFeedImageS3Key()))
                .toList();
        String profileImageUrl = fileService.getPreSignedUrl(feedDetailDtoList.getFirst().getProfileS3Key());

        // groupBy로 묶고 dto에 넣어서 반환
        return FeedDetailResponse.from(feedDetailDtoList.getFirst(), profileImageUrl,imageUrls);
    }

    public FeedPageItemResponse getFeedPreviews(Long userId , Long cursor, boolean isMine, int limit, Long targetId) {
        //다중 조인으로 전체 조회(프로필, 피드 ,이미지들, 좋아요여부)
        FeedPageItemResponse feedPageItemResponse = feedRepository.findFeedPreviewsWithImageAndIsLiked( userId, cursor,isMine, limit, false, targetId);
        // 이미지 s3Key로 s3 조회해서 url 획득 + 프로필 이미지도 획득  
        feedPageItemResponse.getFeeds().forEach(feedPreviewResponse -> {
            feedPreviewResponse.setImageUrl(fileService.getPreSignedUrl(feedPreviewResponse.getImageUrl()));
        });
        return feedPageItemResponse;
    }

    public FeedRankingPageItemResponse getRankingFeedPreviews(Long userId , Long cursor, int limit) {
        //다중 조인으로 전체 조회(프로필, 피드 ,이미지들, 좋아요여부)
        FeedRankingPageItemResponse feedRankingPageItemResponse = feedRepository.findRankingFeedPreviewsWithImageAndIsLiked( userId, cursor, limit);
        // 이미지 s3Key로 s3 조회해서 url 획득 + 프로필 이미지도 획득
        feedRankingPageItemResponse.getFeeds().forEach(feedPreviewResponse -> {
            feedPreviewResponse.setImageUrl(fileService.getPreSignedUrl(feedPreviewResponse.getImageUrl()));
        });
        return feedRankingPageItemResponse;
    }
}
