package com.threeboys.toneup.user.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.CustomOAuth2User;
import com.threeboys.toneup.user.dto.ProfileResponse;
import com.threeboys.toneup.user.dto.UpdateProfileRequest;
import com.threeboys.toneup.user.service.Userservice;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserController {

    private final Userservice userservice;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        ProfileResponse profileResponse = userservice.getProfile(userId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", profileResponse));
    }
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId){
        ProfileResponse profileResponse = userservice.getProfile(userId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", profileResponse));
    }

    @PatchMapping
    public ResponseEntity<?> changeProfile(@RequestBody UpdateProfileRequest updateProfileRequest, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        userservice.updateProfile(userId, updateProfileRequest);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", Collections.emptyMap()));
    }
}
