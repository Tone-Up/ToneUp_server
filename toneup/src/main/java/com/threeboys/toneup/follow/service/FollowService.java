package com.threeboys.toneup.follow.service;

import com.threeboys.toneup.follow.domain.UserFollow;
import com.threeboys.toneup.follow.dto.FollowResponse;
import com.threeboys.toneup.follow.repository.UserFollowRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowResponse followUser(Long userId, Long targetUserId){

        Optional<UserFollow> userFollowEntity  = userFollowRepository.findByFollowerIdAndFolloweeId(userId, targetUserId);
        if(userFollowEntity.isEmpty()){
            UserEntity user = userRepository.getReferenceById(userId);
            UserEntity targetUser = userRepository.getReferenceById(targetUserId);
            UserFollow userFollow = new UserFollow(user,targetUser);
            userFollowRepository.save(userFollow);
        }else{
            userFollowRepository.delete(userFollowEntity.get());
        }
        boolean isFollower = userFollowRepository.existsByFollowerIdAndFolloweeId(targetUserId, userId);
        boolean isFollowing = userFollowRepository.existsByFollowerIdAndFolloweeId(userId, targetUserId);
        return new FollowResponse(isFollower, isFollowing);
    }

//    @Transactional
//    public void unFollowUser(Long userId,Long targetUserId){
//        userFollowRepository.deleteByFollowerIdAndFolloweeId(userId, targetUserId);
//    }
}
