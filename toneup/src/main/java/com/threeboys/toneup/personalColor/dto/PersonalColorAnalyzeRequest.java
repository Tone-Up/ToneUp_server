package com.threeboys.toneup.personalColor.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
public class PersonalColorAnalyzeRequest {
    private final Long userId;
    private final MultipartFile image;
}
