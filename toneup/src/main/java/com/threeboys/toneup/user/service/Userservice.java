package com.threeboys.toneup.user.service;

import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.diary.repository.DiaryRepository;
import com.threeboys.toneup.feed.repository.FeedRepository;
import com.threeboys.toneup.follow.repository.UserFollowRepository;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.domain.User;
import com.threeboys.toneup.user.dto.ProfileResponse;
import com.threeboys.toneup.user.dto.UpdateProfileRequest;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.DuplicateNicknameException;
import com.threeboys.toneup.user.exception.UserNotFoundException;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class Userservice {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final UserFollowRepository followRepository;
    private final DiaryRepository diaryRepository;
    private final FeedRepository feedRepository;
    private final FileService fileService;

    private static final String DEFAULT_PROFILE_IMAGE ="images/DEFAULT_PROFILE_IMAGE";

    public UserEntity registerUser(String name, String nickname, String email, ProviderType providerType, String providerId){
        return userRepository.findByProviderAndProviderId(providerType,providerId)
//                .map(userEntity -> {
                    // 정보 업데이트
//                    userEntity.setName(name);
//                    userEntity.setEmail(email);
//                    return userRepository.save(userEntity);
//                })
                .orElseGet(() -> {
                    // 기본 프로필 이미지 추가(기본 프로필 이미지 id = 0)
                    // 이미지 없을때 예외 처리 해야하나
                    Images profileImage = Images.builder()
                        .url(DEFAULT_PROFILE_IMAGE)
                        .type(ImageType.PROFILE)
//                        .refId()
                        .order(0)
                        .s3Key(DEFAULT_PROFILE_IMAGE)
                        .build();

                    // 회원가입
                    UserEntity newUser = new UserEntity(name,nickname, providerType, providerId, email, profileImage);
                    return userRepository.save(newUser);
                });
    }


    public boolean isPersonal(UserEntity user) {
        return user.getPersonalColor() != null;
    }

    public boolean isSignedUp(UserEntity user) {
        return user.getId() != null;
    }

    public ProfileResponse getProfile(Long userId, Long targetId) {
        UserEntity targetEntity = userRepository.findById(targetId).orElseThrow(()->new UserNotFoundException(userId));
        Long followerCount = followRepository.countByFolloweeId(targetId);
        Long followingCount = followRepository.countByFollowerId(targetId);
        Long diaryCount = null;
        if(userId.equals(targetId)){
            diaryCount = diaryRepository.countByUserId(userId);
        }
        Long feedCount = feedRepository.countByUserId(userId);

        boolean isFollowing = followRepository.existsByFollowerIdAndFolloweeId(userId, targetId);
        boolean isFollower = followRepository.existsByFollowerIdAndFolloweeId(targetId, userId);

        String profileUrl = fileService.getPreSignedUrl(targetEntity.getProfileImageId().getS3Key());
        return ProfileResponse.from(targetEntity, profileUrl, followerCount, followingCount, isFollower,isFollowing, feedCount, diaryCount);
    }

    public void updateProfile(Long userId, UpdateProfileRequest updateProfileRequest) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(userId));
        User user = userEntity.toDomain();
        System.out.println(userEntity.getProfileImageId());

        Long profileId = userEntity.getProfileImageId().getId();
        String s3Key = userEntity.getProfileImageId().getS3Key();
        //Mapper로 변경 고려 필요? or changeProfile 안에 넣기?
        if(updateProfileRequest.getNickname()!=null&&!updateProfileRequest.getNickname().equals(user.getNickname())) {
            // 중복 닉네임 아닌 경우에 닉네임 검증(길이 및 특수문자) 후 변경
            userRepository.findByNickname(updateProfileRequest.getNickname())
                    .ifPresent(userEntity1 -> {
                        throw new DuplicateNicknameException();
                    });
            user.changNickname(updateProfileRequest.getNickname());
        }
        if(updateProfileRequest.getBio()!=null) user.changeBio(updateProfileRequest.getBio());
        if(updateProfileRequest.getProfileImageUrl()!=null) user.changeProfileImage(updateProfileRequest.getProfileImageUrl());

        //s3 기존 이미지 삭제 필요?(기본 이미지였던 경우에는 제외) fileService.changeProfileImage(?)
        //기본 프로필 이미지가 아닌 경우 현재 이미지 엔티티 s3Key값만 수정
        if(!userEntity.getProfileImageId().getS3Key().equals(DEFAULT_PROFILE_IMAGE)) fileService.deleteS3Object(s3Key);

        userEntity.changeProfile(user);

//        Images image = userEntity.getProfileImageId();
//        image.changeProfileImageUrl(user.getProfileImageUrl());

        //더티 체킹으로 이미지 url 변경
//        Images image = Images.builder()
//                .url(user.getProfileImageUrl())
//                .type(ImageType.PROFILE)
//                .refId(userId)
//                .order(0)
//                .s3Key(user.getProfileImageUrl())
//                .build();


    }
}
