package com.threeboys.toneup.socketio.domain;

import com.threeboys.toneup.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    private String token;
    private String deviceType;
    private boolean isActive;

    public void changeIsActive(boolean status) {
        this.isActive = status;
    }
}
