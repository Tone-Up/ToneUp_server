package com.threeboys.toneup.feed.service;

import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.feed.dto.FeedDetailResponse;
import com.threeboys.toneup.feed.dto.FeedRequest;
import com.threeboys.toneup.feed.dto.FeedResponse;
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
    void changeFeed(){
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
        Long feedId = 100L;
        Long userId = 1L;

//        FeedDetailResponse feedResponse = feedService.getFeed(feedId);


    }
}