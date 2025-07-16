package com.threeboys.toneup.diary.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.diary.dto.DiaryDetailResponse;
import com.threeboys.toneup.diary.dto.DiaryPageItemResponse;
import com.threeboys.toneup.diary.dto.DiaryRequest;
import com.threeboys.toneup.diary.dto.DiaryResponse;
import com.threeboys.toneup.diary.service.DiaryService;
import com.threeboys.toneup.feed.dto.*;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping("/my-diary")
    public ResponseEntity<?> createDiary(@RequestBody DiaryRequest diaryRequest, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        DiaryResponse diaryResponse = diaryService.createDiary(userId, diaryRequest);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",diaryResponse));
    }

    @GetMapping("/my-diary/{diaryId}")
    public ResponseEntity<?> getDiaryDetail(@PathVariable Long diaryId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        DiaryDetailResponse diaryDetailResponse = diaryService.getDiary(userId, diaryId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",diaryDetailResponse));
    }
    @GetMapping("/my-diary")
    public ResponseEntity<?> getDiaryPagination(@RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "false") boolean isMine, @RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        DiaryPageItemResponse diaryPageItemResponse = diaryService.getDiaryPreviews(userId, cursor,isMine, limit);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok", diaryPageItemResponse));
    }
    @PutMapping("/my-diary/{diaryId}")
    public ResponseEntity<?> updateDiary(@PathVariable Long diaryId, @RequestBody DiaryRequest diaryRequest, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        DiaryResponse diaryResponse = diaryService.updateDiary(userId, diaryId, diaryRequest);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",diaryResponse));
    }
    @DeleteMapping("/my-diary/{diaryId}")
    public ResponseEntity<?> deleteDiary(@PathVariable Long diaryId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        diaryService.deleteDiary(userId, diaryId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok",null));
    }
}
