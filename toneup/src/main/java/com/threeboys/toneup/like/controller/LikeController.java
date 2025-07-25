package com.threeboys.toneup.like.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.feed.dto.FeedPageItemResponse;
import com.threeboys.toneup.like.dto.FeedLikeResponse;
import com.threeboys.toneup.like.dto.ProductLikeResponse;
import com.threeboys.toneup.like.service.LikeService;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/feeds/like")
    public ResponseEntity<?> getLikeFeedPreviews(@RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "true") boolean isMine, @RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        FeedPageItemResponse feedPageItemResponse = likeService.getLikeFeedPreviews(userId, cursor, isMine, limit);
        return ResponseEntity.ok(new StandardResponse<>(true, 200, "ok", feedPageItemResponse));
    }

    @GetMapping("/product/like")
    public ResponseEntity<?> getLikeProductPreviews(@RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "true") boolean isMine, @RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        ProductPageItemResponse productPageItemResponse = likeService.getLikeProductPreviews(userId, cursor, isMine, limit);
        return ResponseEntity.ok(new StandardResponse<>(true, 200, "ok", productPageItemResponse));
    }
}
