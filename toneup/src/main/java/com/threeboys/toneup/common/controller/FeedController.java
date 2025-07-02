package com.threeboys.toneup.common.controller;

import com.threeboys.toneup.common.response.PresignedUrlListResponseDTO;
import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.feed.dto.FeedRequest;
import com.threeboys.toneup.feed.dto.FeedResponse;
import com.threeboys.toneup.feed.service.FeedService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
