package com.threeboys.toneup.like.service;

import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.feed.exception.FeedNotFoundException;
import com.threeboys.toneup.feed.repository.FeedRepository;
import com.threeboys.toneup.like.annotation.DistributedLock;
import com.threeboys.toneup.like.domain.FeedsLike;
import com.threeboys.toneup.like.domain.ProductsLike;
import com.threeboys.toneup.like.dto.FeedLikeResponse;
import com.threeboys.toneup.like.dto.ProductLikeResponse;
import com.threeboys.toneup.like.repository.FeedsLikeRepository;
import com.threeboys.toneup.like.repository.ProductsLikeRepository;
import com.threeboys.toneup.product.domain.Product;
import com.threeboys.toneup.product.exception.ProductNotFoundException;
import com.threeboys.toneup.product.repository.ProductRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {
    private final FeedRepository feedRepository;
    private final ProductRepository productRepository;
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
            if(!lock.tryLock(2,3, TimeUnit.SECONDS)) {
                throw new RuntimeException("lock 획득 실패!!!");
            }

            boolean isLiked = feedsLikeRepository.existsByFeedIdAndUserId(feedId, userId);
            //좋아요 테이블에 존재하면 삭제 후 피드 테이블 likeCount - 1
            Feed feed = feedRepository.findById(feedId).orElseThrow(FeedNotFoundException::new);
            if(isLiked){
//                feedRepository.flush();
                feedsLikeRepository.deleteByFeedIdAndUserId(feedId, userId);
                feed.decreaseLikeCount();

//                feedsLikeRepository.flush();
            }else{ //좋아요 테이블에 존재하지 않으면 추가 후 피드 테이블 likeCount + 1
                UserEntity user = userRepository.getReferenceById(userId);
                FeedsLike feedsLike = FeedsLike.builder()
                        .feed(feed)
                        .user(user)
                        .build();
//                feedRepository.flush();
                feedsLikeRepository.save(feedsLike);
                feed.increaseLikeCount();

//                feedsLikeRepository.flush();
            }



            // 트랜잭션 커밋 이후에 락 해제
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            });

        }catch (InterruptedException ex){
//            Thread.currentThread().interrupt(); //사용 이유 다시 알아보기
            log.error(ex.getMessage());
        }
//        finally {
//            if(lock != null && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
//        }

    }
    //aop 적용
//    @Transactional
    @DistributedLock(key = "'FEED_'+ #feedId")
    public FeedLikeResponse feedToggleLike(Long feedId, Long userId){
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
            return new FeedLikeResponse(feedId, !isLiked);

    }
//    @Transactional
    //굳이 분산락 필요한가 동일 유저가 동시 요청을 하는 경우가 많지 않을꺼 같은데 낙관락 적용해도 되지 않나
    @DistributedLock(key = "'PRODUCT_' + #productId + '_' + #userId")
    public ProductLikeResponse productToggleLike(Long productId, Long userId){
        boolean isLiked = productsLikeRepository.existsByProductIdAndUserId(productId, userId);

        Product product = productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
        //좋아요 테이블에 존재하면 삭제
        if(isLiked){
            productsLikeRepository.deleteByProductIdAndUserId(productId, userId);

        }else{ //좋아요 테이블에 존재하지 않으면 추가
            UserEntity user = userRepository.getReferenceById(userId);
            ProductsLike productsLike = ProductsLike.builder()
                    .product(product)
                    .user(user)
                    .build();
            productsLikeRepository.save(productsLike);
        }
        return new ProductLikeResponse(productId, !isLiked);
    }

}
