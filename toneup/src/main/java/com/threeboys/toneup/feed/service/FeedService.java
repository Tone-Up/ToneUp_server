package com.threeboys.toneup.feed.service;

import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.feed.dto.FeedRequest;
import com.threeboys.toneup.feed.dto.FeedResponse;
import com.threeboys.toneup.feed.repository.FeedRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

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
}
