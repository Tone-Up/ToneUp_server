package com.threeboys.toneup.user.service;

import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.diary.repository.DiaryRepository;
import com.threeboys.toneup.feed.repository.FeedRepository;
import com.threeboys.toneup.follow.repository.UserFollowRepository;
import com.threeboys.toneup.personalColor.domain.PersonalColor;
import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.domain.User;
import com.threeboys.toneup.user.dto.ProfileResponse;
import com.threeboys.toneup.user.dto.UpdateProfileRequest;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private Userservice userservice;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFollowRepository followRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private FileService fileService;

    @Test
    @DisplayName("프로필_조회")
    void getProfile(){
        Images images = Images.builder()
                .s3Key("some-s3-key")
                .build();
        UserEntity userEntity = new UserEntity("김준영", "Jjun", ProviderType.GOOGLE, "test1234", "test1234@test.com",images);
        userEntity.updatePersonalColor(PersonalColor.builder().personalColorType(PersonalColorType.AUTUMN).build());
        Long userId = 1L;
        String profileImageUrl = "http://test";


        when(fileService.getPreSignedUrl(images.getS3Key())).thenReturn(profileImageUrl);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(followRepository.countByFolloweeId(userId)).thenReturn(0L);
        when(followRepository.countByFollowerId(userId)).thenReturn(0L);
       when(followRepository.existsByFollowerIdAndFolloweeId(userId, userId)).thenReturn(false);
        when(feedRepository.countByUserId(userId)).thenReturn(0L);
        when(diaryRepository.countByUserId(userId)).thenReturn(0L);
        ProfileResponse expectProfileResponse = ProfileResponse.from(userEntity, profileImageUrl, 0L, 0L, false, false, 0L, 0L);
        ProfileResponse profileResponse = userservice.getProfile(userId, userId);

        assertEquals(expectProfileResponse, profileResponse);
    }
    @Test
    @DisplayName("프로필 수정")
    void updateProfile() {
        // profileImage mock 준비
        Images profileImageMock = mock(Images.class);
        when(profileImageMock.getId()).thenReturn(1L);
        when(profileImageMock.getS3Key()).thenReturn("some-s3-key");

        // UserEntity mock 준비
        UserEntity mockEntity = mock(UserEntity.class);
        when(mockEntity.getProfileImageId()).thenReturn(profileImageMock);

        User mockDomain = mock(User.class);
        when(mockEntity.toDomain()).thenReturn(mockDomain);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockEntity));

        // updateProfileRequest 생성
        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .bio("updateBio")
                .build();

        // when
        userservice.updateProfile(1L, updateProfileRequest);

        // then
        verify(mockDomain).changeBio(updateProfileRequest.getBio());
        verify(fileService).deleteS3Object("some-s3-key");
        verify(mockEntity).changeProfile(mockDomain);
    }

    @Test
    @DisplayName("프로필 동등성 비교")
    void equalsProfile(){
        Images image = new Images();
        UserEntity user1 = new UserEntity("김준영", "Jjun", ProviderType.GOOGLE, "test1234", "test1234@test.com", image);
        UserEntity user2 = new UserEntity("김준영", "Jjun", ProviderType.GOOGLE, "test1234", "test1234@test.com", image);

        assertEquals(user1,user2);
    }
}
