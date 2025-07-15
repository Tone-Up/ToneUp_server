package com.threeboys.toneup.feed.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.feed.dto.*;
import com.threeboys.toneup.feed.service.FeedService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;
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

    @GetMapping("/feeds/{feedId}")
    public ResponseEntity<?> getFeedDetail(@PathVariable Long feedId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        FeedDetailResponse feedDetailResponse = feedService.getFeed(userId, feedId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",feedDetailResponse));
    }
    @GetMapping("/feeds")
    public ResponseEntity<?> getFeedPagination(@RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        FeedPageItemResponse feedPageItemResponse = feedService.getFeedPreviews(userId, cursor, limit);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",feedPageItemResponse));
    }
    @GetMapping("/rankingfeeds")
    public ResponseEntity<?> getFeedRankingPagination(@RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        FeedRankingPageItemResponse feedRankingPageItemResponse = feedService.getRankingFeedPreviews(userId, cursor, limit);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",feedRankingPageItemResponse));
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
