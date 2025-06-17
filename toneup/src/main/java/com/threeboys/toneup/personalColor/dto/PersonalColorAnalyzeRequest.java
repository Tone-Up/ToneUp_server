package com.threeboys.toneup.personalColor.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Data
public class PersonalColorAnalyzeRequest {
    private final Long userId;
    private final MultipartFile image;
}
