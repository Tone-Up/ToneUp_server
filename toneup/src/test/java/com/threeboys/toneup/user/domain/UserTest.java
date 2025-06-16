package com.threeboys.toneup.user.domain;

import com.threeboys.toneup.user.exception.InvalidNicknameException;
import com.threeboys.toneup.user.exception.InvalidProfileImageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("User  도메인 테스트")
public class UserTest {
    @Test
    void changeNickname(){
        User user = new User("beforNickname");
        user.changNickname("afterNickname");
        assertEquals("afterNickname", user.getNickname());
    }
    @Test
    void changeNickname_lengthTooLong(){
        User user = new User("beforNickname");
        String longNickname ="afterNickname".repeat(3);
        assertThrows(InvalidNicknameException.class, ()->user.changNickname(longNickname));
    }
    @Test
    void changeNickname_containsSpecialCharacters(){
        User user = new User("beforNickname");
        assertThrows(InvalidNicknameException.class, ()->user.changNickname("afterNickname@^!"));
    }
    @Test
    void changeBio(){
        User user = new User("beforNickname");
        user.changeBio("소개글 변경 완료!");
        assertEquals("소개글 변경 완료!", user.getBio());
    }
    @Test
    void changeBio_lengthTooLong(){
        User user = new User("beforNickname");
        String longBio ="Bio".repeat(50);
        assertThrows(IllegalArgumentException.class, ()->user.changeBio(longBio));
    }
    @Test
    void changeProfileImage(){
        User user = new User("beforNickname");
        String url = "https://example.com/profile.png";
        user.changeProfileImage(url);
        assertThat(user.getProfileImageUrl()).isEqualTo(url);
    }
    @Test
    void changeProfileImage_null(){
        User user = new User("beforNickname");
        assertThatThrownBy(()->user.changeProfileImage(null)).isInstanceOf(InvalidProfileImageException.class);
    }

}
