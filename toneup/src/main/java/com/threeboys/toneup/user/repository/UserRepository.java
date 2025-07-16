package com.threeboys.toneup.user.repository;

import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String mail);
    Optional<UserEntity> findByNickname(String nickname);
    Optional<UserEntity> findByProviderAndProviderId(ProviderType provider, String providerId);
}
