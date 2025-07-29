package com.threeboys.toneup.search.controller;

import com.redislabs.lettusearch.Suggestion;
import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import com.threeboys.toneup.search.service.SearchService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<?> getSearchProductPagination(@RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "false") boolean isMine,
                                                        @RequestParam(defaultValue = "10") int limit, @RequestParam(required = false) String query, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        ProductPageItemResponse productPageItemResponse = searchService.getSearchProduct(userId, query, cursor, limit);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",productPageItemResponse));
    }

    @GetMapping("/auto-complete")
    public ResponseEntity<?> getAutoCompleteList(@RequestParam(required = false) String keyword){
        System.out.println("keyword; " + keyword);
        List<Suggestion<String>> autoComplete = searchService.getAutoComplete(keyword);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",autoComplete));
    }

}
