package com.threeboys.toneup.user.service;

import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Userservice {
    private final UserRepository userRepository;

    public UserEntity registerUser(String name, String nickname, String email, ProviderType providerType, String providerId){
        return userRepository.findByProviderAndProviderId(providerType,providerId)
                .map(userEntity -> {
                    // 정보 업데이트
                    userEntity.setName(name);
                    userEntity.setEmail(email);
                    return userRepository.save(userEntity);
                })
                .orElseGet(() -> {
                    // 회원가입
                    UserEntity newUser = new UserEntity(name,nickname, providerType, providerId, email);
                    return userRepository.save(newUser);
                });
    }


    public boolean isPersonal(UserEntity user) {
        return user.getPersonalColorId() != null;
    }

    public boolean isSignedUp(UserEntity user) {
        return user.getId() != null;
    }

}
