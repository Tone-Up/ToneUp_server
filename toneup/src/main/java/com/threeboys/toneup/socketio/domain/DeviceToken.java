package com.threeboys.toneup.socketio.domain;

import com.threeboys.toneup.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    private String token;
    private String deviceType;
    private boolean isActive;

    public DeviceToken(UserEntity user, String token, String deviceType, boolean isActive) {
        this.user = user;
        this.token = token;
        this.deviceType = deviceType;
        this.isActive = isActive;
    }

    public void changeIsActive(boolean status) {
        this.isActive = status;
    }
}
