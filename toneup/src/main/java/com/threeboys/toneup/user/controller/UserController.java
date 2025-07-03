package com.threeboys.toneup.user.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.CustomOAuth2User;
import com.threeboys.toneup.user.dto.ProfileResponse;
import com.threeboys.toneup.user.service.Userservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserController {

    private final Userservice userservice;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        ProfileResponse profileResponse = userservice.getProfile(userId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", profileResponse));
    }
}
