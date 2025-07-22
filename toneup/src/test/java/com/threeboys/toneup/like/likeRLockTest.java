// package com.threeboys.toneup.like;

// import com.threeboys.toneup.common.domain.ImageType;
// import com.threeboys.toneup.common.domain.Images;
// import com.threeboys.toneup.common.repository.ImageRepository;
// import com.threeboys.toneup.feed.domain.Feed;
// import com.threeboys.toneup.feed.repository.FeedRepository;
// import com.threeboys.toneup.like.repository.FeedsLikeRepository;
// import com.threeboys.toneup.like.repository.ProductsLikeRepository;
// import com.threeboys.toneup.like.service.LikeService;
// import com.threeboys.toneup.product.repository.ProductRepository;
// import com.threeboys.toneup.security.provider.ProviderType;
// import com.threeboys.toneup.user.entity.UserEntity;
// import com.threeboys.toneup.user.repository.UserRepository;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Propagation;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;

// import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest
// public class likeRLockTest {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private FeedRepository feedRepository;


//     @Autowired
//     private ImageRepository imageRepository;

//     @Autowired
//     private FeedsLikeRepository feedsLikeRepository;

//     @Autowired
//     private ProductsLikeRepository productsLikeRepository;

//     @Autowired
//     private LikeService likeService;



//     private Long testFeedId;
//     private Long testStartUserId;
//     private final int TestUserCount=50;
//     private final Long testProductUser = 1L;
//     public static final Long testProductId = 1L;

//     @BeforeEach
// //    @Transactional
//     void setUp(){
//         UserEntity user = userRepository.findById(1L).orElseThrow();
//         Feed feed = Feed.builder().content("Like Test Feed").user(user).build();
//         feedRepository.save(feed);
//         testFeedId = feed.getId();


//         for(long i=0; i<TestUserCount; i++){
//             Images images = Images.builder()
//                     .type(ImageType.PROFILE)
//                     .order(0)
//                     .s3Key("images/DEFAULT_PROFILE_IMAGE")
//                     .build();

//             UserEntity testUser = new UserEntity("likeTestUser"+i, "userNickname"+i, ProviderType.GOOGLE, "testProviderId" +i, "testEmail"+i+"@test.com",images);
//             userRepository.save(testUser);
//             if(i==0) testStartUserId = testUser.getId();
//         }

//         System.out.println("testStartUserId : " + testStartUserId);
//     }

//     @Test
//     @DisplayName("피드 좋아요 api service 메서드 redisson 분산락 테스트")
//     @Transactional(propagation = Propagation.NOT_SUPPORTED)
//     void toggleFeedLikeTestWithRLock() throws InterruptedException {
//         int threadCount = TestUserCount;
//         ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//         CountDownLatch latch = new CountDownLatch(threadCount);

//         for (long i = 1; i <= threadCount; i++) {
//             final long userIdIdx = i;
//             executor.submit(() -> {
//                 try {
// //                    likeService.feedLike(testFeedId, testStartUserId-1 + userIdIdx); // 여기서 그대로 사용
//                     likeService.feedToggleLike(testFeedId, testStartUserId-1 + userIdIdx); // 여기서 그대로 사용
//                 } finally {
//                     latch.countDown();
//                 }
//             });
//         }

//         latch.await();
//         executor.shutdown();

//         Feed afterFeed = feedRepository.findById(testFeedId).orElseThrow();
//         int totalLikeCount = afterFeed.getLikeCount();
//         assertThat(totalLikeCount).isEqualTo(TestUserCount);
//     }

//     @Test
//     @DisplayName("상품 좋아요 api service 메서드 redisson 분산락 테스트(동일 유저가 동시 요청 시)")
//     @Transactional(propagation = Propagation.NOT_SUPPORTED)
//     void toggleProductLikeTestWithRLock() throws InterruptedException {
//         int threadCount = TestUserCount;
//         ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//         CountDownLatch latch = new CountDownLatch(threadCount);

//         for (long i = 0; i < threadCount; i++) {
//             final long userIdIdx = i;
//             executor.submit(() -> {
//                 try {
//                     likeService.productToggleLike(testProductId, testProductUser);//testStartUserId + userIdIdx
//                 } finally {
//                     latch.countDown();
//                 }
//             });
//         }

//         latch.await();
//         executor.shutdown();

// //        long ProductlikeCount = productsLikeRepository.countByProductId(testProductId);
// //        assertThat(ProductlikeCount).isEqualTo(TestUserCount);
//         long ProductlikeCount = productsLikeRepository.countByProductId(testProductId);
//         assertThat(ProductlikeCount).isEqualTo(threadCount%2);

//     }

// //    @AfterEach
// //    @Transactional
// //    void cleanUp() throws InterruptedException {
// //        Thread.sleep(2000);
// //        feedsLikeRepository.deleteAllByFeedId(testFeedId);
// //        userRepository.deleteAllByNameStartingWith("likeTestUser");
// //        feedRepository.deleteById(testFeedId);
// //    }
// }
