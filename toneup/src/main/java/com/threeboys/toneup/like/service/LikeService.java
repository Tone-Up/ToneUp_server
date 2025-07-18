package com.threeboys.toneup.like.service;

import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.feed.exception.FeedNotFoundException;
import com.threeboys.toneup.feed.repository.FeedRepository;
import com.threeboys.toneup.like.domain.FeedsLike;
import com.threeboys.toneup.like.repository.FeedsLikeRepository;
import com.threeboys.toneup.like.repository.ProductsLikeRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    private final FeedsLikeRepository feedsLikeRepository;
    private final ProductsLikeRepository productsLikeRepository;

    private final RedissonClient redissonClient;


    @Transactional
    public void feedLike(Long feedId, Long userId){
        final String lockName = "feedLike:lock:"+feedId;
        final RLock lock = redissonClient.getLock(lockName);
        try{
            //redisson 분산락 획득
            if(!lock.tryLock(1,3, TimeUnit.SECONDS)) return;

            boolean isLiked = feedsLikeRepository.existsByFeedIdAndUserId(feedId, userId);
            //좋아요 테이블에 존재하면 삭제 후 피드 테이블 likeCount - 1
            Feed feed = feedRepository.findById(feedId).orElseThrow(FeedNotFoundException::new);
            if(isLiked){
                feedsLikeRepository.deleteByFeedIdAndUserId(feedId, userId);
                feed.decreaseLikeCount();
            }else{ //좋아요 테이블에 존재하지 않으면 추가 후 피드 테이블 likeCount + 1
                UserEntity user = userRepository.getReferenceById(userId);
                FeedsLike feedsLike = FeedsLike.builder()
                        .feed(feed)
                        .user(user)
                        .build();
                feedsLikeRepository.save(feedsLike);
                feed.increaseLikeCount();
            }
        }catch (InterruptedException ex){
//            Thread.currentThread().interrupt(); //사용 이유 다시 알아보기
            log.error(ex.getMessage());
        }finally {
            if(lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

}
