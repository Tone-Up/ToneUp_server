package com.threeboys.toneup.follow.repository;

import com.threeboys.toneup.follow.domain.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    public  void deleteByFollowerIdAndFolloweeId(Long userId, Long targetUserId);

    Long countByFollowerId(Long userId);

    Long countByFolloweeId(Long userId);
}
