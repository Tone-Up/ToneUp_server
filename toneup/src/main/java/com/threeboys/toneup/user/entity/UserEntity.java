package com.threeboys.toneup.user.entity;

import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.personalColor.domain.PersonalColor;
import com.threeboys.toneup.security.provider.ProviderType;
import com.threeboys.toneup.user.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
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

    @OneToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image_id")
    private Images profileImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_color_id")
    private PersonalColor personalColor;

    @Column(nullable = false)
    private String role;

    @Column(length = 100)
    private String bio;

    public UserEntity(String name, String nickname, ProviderType provider, String providerId, String email, Images image) {
        this.name = name;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.profileImageId = image;
        this.role ="ROLE_USER";
        this.bio ="안녕하세요!";
    }

    public UserEntity(long id) {
        this.id = id;
    }


    public User toDomain() {
        return User.fromEntity(this);
    }

    public void updatePersonalColor(PersonalColor personalColor) {
        this.personalColor = personalColor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserEntity that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(nickname, that.nickname) && provider == that.provider && Objects.equals(providerId, that.providerId) && Objects.equals(email, that.email) && Objects.equals(profileImageId, that.profileImageId) && Objects.equals(personalColor, that.personalColor) && Objects.equals(role, that.role) && Objects.equals(bio, that.bio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, nickname, provider, providerId, email, profileImageId, personalColor, role, bio);
    }

    public void changeProfile(User user) {

        this.nickname  = user.getNickname();
        this.bio =user.getBio();
        this.profileImageId.changeProfileImageUrl(user.getProfileImageUrl());
    }
}
