package com.threeboys.toneup.feed.service;

import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.feed.dto.FeedRequest;
import com.threeboys.toneup.feed.dto.FeedResponse;
import com.threeboys.toneup.feed.repository.FeedRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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


    @Test
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
}