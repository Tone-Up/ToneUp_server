package com.threeboys.toneup.personalColor.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.personalColor.service.PersonalColorService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PersonalColorController {

    private final PersonalColorService personalColorService;

    @PostMapping("/personalcolor")
    public ResponseEntity<?> createPersonalColor(@RequestPart("image") MultipartFile imageFile, @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        Long userId = oAuth2User.getId();
        String personalColor = personalColorService.updatePersonalColor(userId, imageFile);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", personalColor));
    }}
