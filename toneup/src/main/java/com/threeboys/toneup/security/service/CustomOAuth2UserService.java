package com.threeboys.toneup.security.service;

import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.security.CustomOAuth2User;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.security.response.GoogleResponse;
import com.threeboys.toneup.security.response.OAuth2Response;
import com.threeboys.toneup.security.response.NaverResponse;
import com.threeboys.toneup.user.domain.User;
import com.threeboys.toneup.user.dto.UserDTO;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else {

            return null;
        }

        String name = oAuth2Response.getName();
        String email = oAuth2Response.getEmail();
        String providerId = oAuth2Response.getProviderId();
        ProviderType providerType = ProviderType.valueOf(oAuth2Response.getProvider().toUpperCase());
        //이 부분 중복 닉네임 안생기게 랜덤값 부여 메서드 추가
        String nickname = name+"_"+providerId;

        UserEntity socialUser =    userRepository.findByProviderAndProviderId(providerType, oAuth2Response.getProviderId())
//                .map(userEntity -> {
//                    // 정보 업데이트
//                    userEntity.setName(name);
//                    userEntity.setEmail(Email);
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
        String personalColor;
        if (socialUser.getPersonalColor() != null && socialUser.getPersonalColor().getPersonalColorType() != null) {
            personalColor =socialUser.getPersonalColor().getPersonalColorType().toString();
        } else {
            personalColor = "null";
        }
        UserDTO userDTO = UserDTO.of(socialUser.getId(), name, nickname,personalColor, socialUser.getRole(), socialUser.getProvider().toString());
        return new CustomOAuth2User(userDTO);

    }

//    private UserEntity saveOrUpdateUserProfile(UserProfile userProfile) {
//        return userRepository.findByEmail(userProfile.getEmail())
//                .orElseGet(() -> userRepository.save(userProfile.toEntity()));
//    }


//    private String generateUniqueNickname(String baseName) {
//        String nickname;
//        do {
//            nickname = baseName + "_" + UUID.randomUUID().toString().substring(0, 6);
//        } while (userRepository.existsByNickname(nickname));
//        return nickname;
//    }
}