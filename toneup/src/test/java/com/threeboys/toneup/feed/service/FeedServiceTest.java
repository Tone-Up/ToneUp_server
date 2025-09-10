package com.threeboys.toneup.feed.service;

import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.feed.dto.*;
import com.threeboys.toneup.feed.exception.FeedNotFoundException;
import com.threeboys.toneup.feed.repository.FeedRepository;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @InjectMocks
    private FeedService feedService;

    @Mock
    private FeedRepository feedRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private FileService fileService;

    @Test
    @DisplayName("피드 생성하기")
    void createFeed() {
        // given
        Long userId = 1L;
        String content = "testContent";
        List<String> imageUrls = List.of("url1", "url2");
        FeedRequest feedRequest = new FeedRequest(content, imageUrls);
        UserEntity user = new UserEntity(userId);

        when(userRepository.getReferenceById(userId)).thenReturn(user);

        when(feedRepository.save(any(Feed.class))).thenAnswer(invocation -> {
            Feed saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100L); // 실제 서비스 내부 객체에 ID 할당
            return saved;
        });

        // when
        FeedResponse response = feedService.createFeed(userId, feedRequest);

        // then
        assertThat(response.getFeedId()).isEqualTo(100L);
    }
    @Test
    @DisplayName("피드 수정하기")
    void updateFeed(){
        // given
        Long feedId = 100L;
        Long userId = 1L;
        FeedRequest request = new FeedRequest("새로운 내용", List.of("url1", "url2"));
        UserEntity user = new UserEntity(userId);

        Feed feed = Mockito.spy(new Feed(user,"기존 내용")); // 변경 전 피드
        List<Images> imagesList = new ArrayList<>(List.of(
                new Images(100L, "test", ImageType.FEED, 0,"images/test1"),
                new Images(100L, "test1", ImageType.FEED, 1,"images/test2")));
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(imageRepository.findByTypeAndRefId(ImageType.FEED,feedId)).thenReturn(imagesList);

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        // when
        FeedResponse response = feedService.updateFeed(userId, feedId, request);

        // then
        // feed 내용 변경 확인
        verify(feed).changeFeed("새로운 내용", List.of("url1", "url2"));

        // 이미지 삭제 및 저장 확인
        verify(imageRepository).findByTypeAndRefId(ImageType.FEED, feedId);
        verify(imageRepository, times(1)).deleteAll(imagesList);
        verify(fileService, times(2)).deleteS3Object(anyString());
        verify(imageRepository).saveAll(feed.getImageUrlList());

        assertThat(response.getFeedId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("피드 삭제하기")
    void deleteFeed(){
        Long feedId = 100L;
        Long userId = 1L;
        UserEntity user = new UserEntity(userId);

        Feed feed = new Feed(user,"feedContent");

        List<Images> imagesList = new ArrayList<>(List.of(
                new Images(100L, "test", ImageType.FEED, 0,"images/test1"),
                new Images(100L, "test1", ImageType.FEED, 1,"images/test2")));
        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(imageRepository.findByTypeAndRefId(ImageType.FEED,feedId)).thenReturn(imagesList);

        feedService.deleteFeed(userId, feedId);

        //then
        verify(imageRepository, times(1)).findByTypeAndRefId(ImageType.FEED, feedId);
        verify(imageRepository, times(1)).deleteAll(imagesList);
        verify(fileService, times(2)).deleteS3Object(anyString());
        verify(feedRepository, times(1)).delete(any(Feed.class));

    }
    @Test
    @DisplayName("피드 조회하기")
    void getFeed(){
        // given
        Long userId = 1L;
        Long feedId = 100L;

        // FeedDetailDto mock 데이터
        FeedDetailDto dto1 = new FeedDetailDto(feedId, "testFeedContent", userId, "testNickname", "testprofileS3Key", "feed-image-1", 0,false, false);
        FeedDetailDto dto2 = new FeedDetailDto(feedId, "testFeedContent", userId, "testNickname", "testprofileS3Key", "feed-image-2", 0,false, false);

        List<FeedDetailDto> mockDtoList = List.of(dto1, dto2);

        // feedRepository mock 동작 정의
        when(feedRepository.findFeedWithUserAndImageAndIsLiked(feedId, userId))
                .thenReturn(mockDtoList);

        // fileService mock 동작 정의
//        when(fileService.getPreSignedUrl("feed-image-1")).thenReturn("url-1");
//        when(fileService.getPreSignedUrl("feed-image-2")).thenReturn("url-2");
//        when(fileService.getPreSignedUrl("testprofileS3Key")).thenReturn("profile-url");

        when(fileService.getCloudFrontUrl("feed-image-1")).thenReturn("url-1");
        when(fileService.getCloudFrontUrl("feed-image-2")).thenReturn("url-2");
        when(fileService.getCloudFrontUrl("testprofileS3Key")).thenReturn("profile-url");

        // when
        FeedDetailResponse result = feedService.getFeed(userId, feedId);

        // then
        assertThat(result.getWriter().getProfileImageUrl()).isEqualTo("profile-url");
        assertThat(result.getImageUrls()).containsExactly("url-1", "url-2");
        assertThat(result.getContent()).isEqualTo("testFeedContent");

        verify(feedRepository).findFeedWithUserAndImageAndIsLiked(feedId, userId);
        verify(fileService).getCloudFrontUrl("feed-image-1");
        verify(fileService).getCloudFrontUrl("feed-image-2");
        verify(fileService).getCloudFrontUrl("testprofileS3Key");
    }

    @Test
    @DisplayName("최신순_피드_페이지네이션")
    void getFeedPreviews() {
        // given
        Long userId = 1L;
        Long cursor = null;
        Long feedId = 100L;
        boolean isMine = false;
        int limit = 10;
        String IMAGE_KEY_1 = "image-key-1";
        String IMAGE_KEY_2 = "image-key-2";


        FeedPreviewResponse preview1 = new FeedPreviewResponse(feedId,IMAGE_KEY_1, false);
        FeedPreviewResponse preview2 = new FeedPreviewResponse(feedId,IMAGE_KEY_2, true);

        FeedPageItemResponse mockResponse = new FeedPageItemResponse(List.of(preview1,preview2), 2L,false, 2L);

        when(feedRepository.findFeedPreviewsWithImageAndIsLiked(userId, cursor, isMine, limit, false, null))
                .thenReturn(mockResponse);
        when(fileService.getPreSignedUrl(IMAGE_KEY_1)).thenReturn("url-1");
        when(fileService.getPreSignedUrl(IMAGE_KEY_2)).thenReturn("url-2");

        // when
        FeedPageItemResponse result = feedService.getFeedPreviews(userId, cursor, isMine, limit, null);

        // then
        assertThat(result.getFeeds()).extracting(FeedPreviewResponse::getImageUrl)
                .containsExactly("url-1", "url-2");

        verify(feedRepository).findFeedPreviewsWithImageAndIsLiked(userId, cursor, isMine, limit, false, null);
        verify(fileService).getPreSignedUrl(IMAGE_KEY_1);
        verify(fileService).getPreSignedUrl(IMAGE_KEY_2);
    }

    @Test
    @DisplayName("인기순_피드_페이지네이션")
    void getRankingFeedPreviews() {
        // given
        Long userId = 1L;
        Long feedId = 100L;
        Long cursor = null;
        int limit = 5;

        String imageUrl1 = "rank-image-key-1";
        String imageUrl2 = "rank-image-key-2";

        FeedPreviewResponse preview1 = new FeedPreviewResponse(feedId, imageUrl1, false);
        FeedPreviewResponse preview2 = new FeedPreviewResponse(feedId, imageUrl2, true);

        FeedRankingPageItemResponse mockResponse = new FeedRankingPageItemResponse(List.of(preview1,preview2), 2L, false);

        when(feedRepository.findRankingFeedPreviewsWithImageAndIsLiked(userId, cursor, limit))
                .thenReturn(mockResponse);
        when(fileService.getPreSignedUrl(imageUrl1)).thenReturn("rank-url-1");
        when(fileService.getPreSignedUrl(imageUrl2)).thenReturn("rank-url-2");

        // when
        FeedRankingPageItemResponse result = feedService.getRankingFeedPreviews(userId, cursor, limit);

        // then
        assertThat(result.getFeeds()).extracting(FeedPreviewResponse::getImageUrl)
                .containsExactly("rank-url-1", "rank-url-2");

        verify(feedRepository).findRankingFeedPreviewsWithImageAndIsLiked(userId, cursor, limit);
        verify(fileService).getPreSignedUrl(imageUrl1);
        verify(fileService).getPreSignedUrl(imageUrl2);
    }


    @Test
    void 피드_조회_예외(){
        Long nonExistFeedId = 999L;
        Long userId = 1L;

        // Mock: findById 호출 시 빈 Optional 반환
        when(feedRepository.findFeedWithUserAndImageAndIsLiked(nonExistFeedId, userId)).thenReturn(List.of());
        // 예외 검증
        assertThrows(FeedNotFoundException.class,() -> feedService.getFeed(userId, nonExistFeedId));

        // 호출 여부 검증
        verify(feedRepository, times(1)).findFeedWithUserAndImageAndIsLiked(nonExistFeedId, userId);
    }

}