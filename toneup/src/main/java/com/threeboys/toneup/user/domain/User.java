package com.threeboys.toneup.user.domain;

import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.InvalidNicknameException;
import com.threeboys.toneup.user.exception.InvalidProfileImageException;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class User {
    private static final String DEFAULT_BIO = "아직 소개글이 없습니다.";
    private static final int MAX_BIO_LENGTH = 100;
    private static final String BIO_TOO_LONG_MESSAGE = "소개글은 최대 100자까지 입력 가능합니다.";
    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 20;
    private static final String NICKNAME_INVALID_LENGTH_MESSAGE = "닉네임은 2자 이상 20자 이하여야 합니다.";
    private static final String NICKNAME_REGEX="^[a-zA-Z0-9]+$";
    private static final String INVALID_CHAR_MESSAGE = "닉네임은 영문자와 숫자만 사용할 수 있습니다.";
    private static final String NICKNAME_NULL_MESSAGE = "닉네임은 null일 수 없습니다.";
    private static final String INVALID_PROFILE_IMAGE_MESSAGE= "프로필 이미지 URL은 null이나 빈 값일 수 없습니다.";


    private String name;
    private String email;
    private String role;
    private String nickname;
    private String personalColor;
    private String bio;
    private String profileImageUrl;
    public User(String nickname){
        this.nickname = nickname;
        this.personalColor = personalColor;
        this.bio = DEFAULT_BIO;
    }

    public User(String name, String email, String role, String nickname, String personalColor, String bio, String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.nickname = nickname;
        this.personalColor = personalColor;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    public void changNickname(String nickname) {
        validateNickname(nickname);
        this.nickname = nickname;
    }

    public void changeBio(String bio) {
        if(bio.length()>MAX_BIO_LENGTH) throw new IllegalArgumentException(BIO_TOO_LONG_MESSAGE);
        this.bio = bio;
    }
    public void changeProfileImage(String newUrl) {
        if(newUrl==null||newUrl.isBlank()) throw new InvalidProfileImageException(INVALID_PROFILE_IMAGE_MESSAGE);
        this.profileImageUrl = newUrl;
    }

    private void validateNickname(String nickname) {
        if(nickname==null){
            throw new InvalidNicknameException(NICKNAME_NULL_MESSAGE);
        }
        if(nickname.length()<MIN_NICKNAME_LENGTH||nickname.length()>MAX_NICKNAME_LENGTH){
            throw new InvalidNicknameException(NICKNAME_INVALID_LENGTH_MESSAGE);
        }
        if(!nickname.matches(NICKNAME_REGEX)){
            throw new InvalidNicknameException(INVALID_CHAR_MESSAGE);
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getPersonalColor() {
        return personalColor;
    }

    public String getBio() {
        return bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public static User fromEntity(UserEntity entity) {
        return new User(entity.getName(),  entity.getEmail(), entity.getRole(), entity.getNickname(),entity.getPersonalColorId(),entity.getBio(),entity.getProfileImageId());
    }

//    public UserEntity toEntity() {
//        return new UserEntity(
//                this.username,
//                this.email,
//                this.name,
//                this.role,
//        );
//    }
}
