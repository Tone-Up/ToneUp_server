package com.threeboys.toneup.user.service;

import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.domain.User;
import com.threeboys.toneup.user.dto.ProfileResponse;
import com.threeboys.toneup.user.dto.UpdateProfileRequest;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.UserNotFoundException;
import com.threeboys.toneup.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class Userservice {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService;

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
                    Images profileImage = imageRepository.findById(0L).orElseThrow();
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

    public ProfileResponse getProfile(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(userId));
        int followerCount = 0 ;// followRepository.countByFolloweeId(userId);
        int followingCount =0 ;// followRepository.countByFollowerId(userId);
        String profileUrl = fileService.getPreSignedUrl(userEntity.getProfileImageId().getS3Key());
        return ProfileResponse.from(userEntity, profileUrl, followerCount, followingCount);
    }

    public void updateProfile(Long userId, UpdateProfileRequest updateProfileRequest) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(userId));
        User user = userEntity.toDomain();
        Long profileId = userEntity.getProfileImageId().getId();
        String s3Key = userEntity.getProfileImageId().getS3Key();
        //Mapper로 변경 고려 필요? or changeProfile 안에 넣기?
        if(updateProfileRequest.getNickname()!=null) user.changNickname(updateProfileRequest.getNickname());
        if(updateProfileRequest.getBio()!=null) user.changeBio(updateProfileRequest.getBio());
        if(updateProfileRequest.getProfileImageUrl()!=null) user.changeProfileImage(updateProfileRequest.getProfileImageUrl());

        //s3 기존 이미지 삭제 필요?(기본 이미지였던 경우에는 제외) fileService.changeProfileImage(?)
        if(profileId!=0)fileService.deleteS3Object(s3Key);

        //더티 체킹으로 이미지 url 변경
        userEntity.changeProfile(user);
    }
}
