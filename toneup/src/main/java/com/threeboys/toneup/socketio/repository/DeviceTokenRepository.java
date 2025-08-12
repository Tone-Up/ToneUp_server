package com.threeboys.toneup.socketio.repository;

import com.threeboys.toneup.socketio.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    @Query("Select d.token From DeviceToken d")
    List<String> findActiveTokensByRoomId(@Param("roomId") Long roomId);

    @Query("Select d.token From DeviceToken d where d.user.id in :userIds and d.isActive = true")
    List<String> findActiveTokensByUserIds(Set<Long> userIds);

    List<DeviceToken> findByUserId(Long userId);
}