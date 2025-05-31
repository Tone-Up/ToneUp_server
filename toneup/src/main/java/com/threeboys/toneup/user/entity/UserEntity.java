package com.threeboys.toneup.user.entity;

import com.threeboys.toneup.personalColor.domain.PersonalColor;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private ProviderType provider;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String email;

    //    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
//    @JoinColumn(name = "profile_image_id")
    private String profileImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_color_id")
    private PersonalColor personalColor;

    @Column(nullable = false)
    private String role;

    @Column(length = 100)
    private String bio;

    public UserEntity(String name, String nickname, ProviderType provider, String providerId, String email) {
        this.name = name;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.role ="ROLE_USER";
        this.bio ="안녕하세요!";
    }

    public UserEntity() {

    }

    public User toDomain() {
        return User.fromEntity(this);
    }

    public void updatePersonalColor(PersonalColor personalColor) {
        this.personalColor = personalColor;
    }
}
