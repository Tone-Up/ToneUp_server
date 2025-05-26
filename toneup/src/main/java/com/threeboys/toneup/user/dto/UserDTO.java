package com.threeboys.toneup.user.dto;

import com.threeboys.toneup.security.provider.ProviderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {
    private Long id;
    private String name;
    private String nickname;
    private String personalColor;
    private String role;
    private String provider;

    private UserDTO(Long id, String name, String nickname, String personalColor, String role, String provider) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.personalColor = personalColor;
        this.role = role;
        this.provider = provider;
    }

    public UserDTO() {
    }

    public static UserDTO of(Long id, String name, String nickname, String personalColor, String role, String provider) {
        return new UserDTO(id, name, nickname, personalColor, role, provider);
    }
}