package com.threeboys.toneup.follow.service;

import com.threeboys.toneup.follow.domain.UserFollow;
import com.threeboys.toneup.follow.repository.UserFollowRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    public void followUser(Long userId,Long targetUserId){
        UserEntity user = userRepository.getReferenceById(userId);
        UserEntity targetUser = userRepository.getReferenceById(targetUserId);
        UserFollow userFollow = new UserFollow(user,targetUser);
        userFollowRepository.save(userFollow);
    }
    @Transactional
    public void unFollowUser(Long userId,Long targetUserId){
        userFollowRepository.deleteByFollowerIdAndFolloweeId(userId, targetUserId);
    }
}
