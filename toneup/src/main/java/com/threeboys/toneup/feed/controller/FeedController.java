package com.threeboys.toneup.feed.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.feed.dto.FeedDetailResponse;
import com.threeboys.toneup.feed.dto.FeedRequest;
import com.threeboys.toneup.feed.dto.FeedResponse;
import com.threeboys.toneup.feed.service.FeedService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FeedController {
    private final FeedService feedService;

    @PostMapping("/feeds")
    public ResponseEntity<?> createFeed(@RequestBody FeedRequest feedRequest, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        FeedResponse feedResponse = feedService.createFeed(userId, feedRequest);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",feedResponse));
    }

    @GetMapping("/feed/{feedId}")
    public ResponseEntity<?> getFeed(@PathVariable Long feedId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        FeedDetailResponse feedDetailResponse = feedService.getFeed(userId, feedId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",feedDetailResponse));
    }

    @PutMapping("/feeds/{feedId}")
    public ResponseEntity<?> updateFeed(@PathVariable Long feedId, @RequestBody FeedRequest feedRequest, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        FeedResponse feedResponse = feedService.updateFeed(userId, feedId, feedRequest);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",feedResponse));
    }
    @DeleteMapping("/feeds/{feedId}")
    public ResponseEntity<?> deleteFeed(@PathVariable Long feedId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        feedService.deleteFeed(userId, feedId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",null));
    }
}
