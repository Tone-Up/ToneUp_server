package com.threeboys.toneup.follow.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.follow.dto.FollowRequest;
import com.threeboys.toneup.follow.service.FollowService;
import com.threeboys.toneup.security.CustomOAuth2User;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FollowController {
    private final FollowService followService;

    @PostMapping("/friends")
    public ResponseEntity<?> follow(@RequestBody FollowRequest followRequest, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        Long targetUserId = followRequest.getTargetUserId();
        followService.followUser(userId, targetUserId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok", Collections.emptyMap()));
    }
    @DeleteMapping("/friends/{friendUserId}")
    public ResponseEntity<?> unFollow(@PathVariable Long friendUserId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        followService.unFollowUser(userId, friendUserId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok", Collections.emptyMap()));
    }
}
