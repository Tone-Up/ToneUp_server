package com.threeboys.toneup.recommand.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.feed.dto.FeedPageItemResponse;
import com.threeboys.toneup.feed.service.FeedService;
import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import com.threeboys.toneup.recommand.service.RecommandService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecommandController {
    private final RecommandService recommandService;

    @GetMapping("/product/recommandation")
    public ResponseEntity<?> getFeedPagination(@RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "false") boolean isMine, @RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        PersonalColorType personalColorType = PersonalColorType.valueOf(customOAuth2User.getPersonalColor());
        ProductPageItemResponse productPageItemResponse = recommandService.getProductItemPagination(userId,personalColorType, cursor, limit);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",productPageItemResponse));
    }



}
