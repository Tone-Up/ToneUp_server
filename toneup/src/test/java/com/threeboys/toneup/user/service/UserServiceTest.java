package com.threeboys.toneup.user.service;

import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.personalColor.domain.PersonalColor;
import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.domain.User;
import com.threeboys.toneup.user.dto.ProfileResponse;
import com.threeboys.toneup.user.dto.UpdateProfileRequest;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.UserNotFoundException;
import com.threeboys.toneup.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Test
    @DisplayName("프로필_조회")
    void getProfile(){
        UserEntity userEntity = new UserEntity("김준영", "Jjun", ProviderType.GOOGLE, "test1234", "test1234@test.com",new Images());
        userEntity.updatePersonalColor(PersonalColor.builder().personalColorType(PersonalColorType.ATUMN).build());
        Long userId = 1L;
        int followerCount = 0 ;// followRepository.countByFolloweeId(userId);
        int followingCount =0 ;// followRepository.countByFollowerId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        ProfileResponse expectProfileResponse = ProfileResponse.from(userEntity, followerCount, followingCount);
        ProfileResponse profileResponse = userservice.getProfile(userId);

        assertEquals(expectProfileResponse, profileResponse);
    }
    @Test
    @DisplayName("프로필 수정")
    void updateProfile(){

        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .bio("updateBio")
                .build();


        UserEntity mockEntity = mock(UserEntity.class);
        User mockDomain = mock(User.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockEntity));
        when(mockEntity.toDomain()).thenReturn(mockDomain);

        // when
        userservice.updateProfile(1L, updateProfileRequest);

        // then
        verify(mockDomain).changeBio("updateBio");
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
