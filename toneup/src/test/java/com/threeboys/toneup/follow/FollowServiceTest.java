//package com.threeboys.toneup.follow;
//
//import com.threeboys.toneup.follow.dto.FollowResponse;
//import com.threeboys.toneup.follow.service.FollowService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Transactional
//public class FollowServiceTest {
//
//    @Autowired
//    private FollowService followService;
//
//    @Test
//    @DisplayName("언팔로우_팔로우_테스트")
//    void followUserTest(){
//        Long userId = 1L;
//        Long targetId = 2L;
//        //언팔로우
//        FollowResponse followResponse = followService.followUser(userId, targetId);
//        assertThat(followResponse.isFollower()).isFalse();
//        assertThat(followResponse.isFollowing()).isFalse();
//
//        //팔로우
//        FollowResponse followResponseAgain = followService.followUser(userId, targetId);
//        assertThat(followResponseAgain.isFollower()).isFalse();
//        assertThat(followResponseAgain.isFollowing()).isTrue();
//
//    }
//}
