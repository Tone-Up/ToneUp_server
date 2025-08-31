package com.threeboys.toneup.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.threeboys.toneup.follow.dto.FollowResponse;
import com.threeboys.toneup.user.entity.UserEntity;
import lombok.*;

import java.util.Objects;
@Data
@NoArgsConstructor
public class ProfileResponse {
    private Long userId;
    private String nickname;
    private String personalColor;
    private String profileImageUrl;
    private String bio;
    private Long follower;
    private Long following;
    @JsonProperty("isFollowing")
    private boolean isFollowing;
    @JsonProperty("isFollower")
    private boolean isFollower;

    private Long feedCount;
    @JsonInclude(JsonInclude.Include.NON_NULL) // null 값인 필드는 JSON에서 제외
    private Long diaryCount;


//    private FollowResponse followResponse;

    @Builder(access = AccessLevel.PRIVATE)
    public ProfileResponse(Long userId, String nickname, String personalColor, String profileImageUrl, String bio, Long follower, Long following, boolean isFollower, boolean isFollowing, Long feedCount, Long diaryCount) {
        this.userId = userId;
        this.nickname = nickname;
        this.personalColor = personalColor;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
        this.follower = follower;
        this.following = following;
        this.isFollower = isFollower;
        this.isFollowing = isFollowing;
        this.feedCount = feedCount;
        this.diaryCount = diaryCount;
    }

    public static ProfileResponse from(UserEntity userEntity, String profileImageUrl, Long follower, Long following, boolean isFollower, boolean isFollowing, Long feedCount, Long diaryCount ) {
        return ProfileResponse.builder()
                .userId(userEntity.getId())
                .nickname(userEntity.getNickname())
                .personalColor(userEntity.getPersonalColor().getPersonalColorType().toString())
                .bio(userEntity.getBio())
                .follower(follower)
                .following(following)
                .isFollowing(isFollowing)
                .isFollower(isFollower)
                .profileImageUrl(profileImageUrl)
                .feedCount(feedCount)
                .diaryCount(diaryCount)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProfileResponse that)) return false;
        return follower == that.follower && following == that.following && Objects.equals(userId, that.userId) && Objects.equals(nickname, that.nickname) && Objects.equals(personalColor, that.personalColor) && Objects.equals(profileImageUrl, that.profileImageUrl) && Objects.equals(bio, that.bio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, nickname, personalColor, profileImageUrl, bio, follower, following);
    }
}
