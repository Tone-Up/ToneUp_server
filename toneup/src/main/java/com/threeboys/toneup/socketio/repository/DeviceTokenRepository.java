package com.threeboys.toneup.socketio.repository;

import com.threeboys.toneup.socketio.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    @Query("Select d.token From DeviceToken d")
    List<String> findActiveTokensByRoomId(@Param("roomId") Long roomId);
}