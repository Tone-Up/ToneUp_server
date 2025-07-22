package com.threeboys.toneup.like.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.like.dto.FeedLikeResponse;
import com.threeboys.toneup.like.dto.ProductLikeResponse;
import com.threeboys.toneup.like.service.LikeService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikeController {
    private final LikeService likeService;


    @PostMapping("/products/{productId}/like")
    public ResponseEntity<?> toggleProductLike(@PathVariable Long productId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        ProductLikeResponse productLikeResponse = likeService.productToggleLike(productId, userId);
        return ResponseEntity.ok(new StandardResponse<>(true, 200, "ok", productLikeResponse));
    }

    @PostMapping("/feeds/{feedId}/like")
    public ResponseEntity<?> toggleFeedLike(@PathVariable Long feedId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        FeedLikeResponse feedLikeResponse = likeService.feedToggleLike(feedId, userId);
        return ResponseEntity.ok(new StandardResponse<>(true, 200, "ok", feedLikeResponse));
    }
}
