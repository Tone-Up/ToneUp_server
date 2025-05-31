package com.threeboys.toneup.personalColor.dto;

import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@RequiredArgsConstructor
public class PersonalColorAnalyzeResponse {
    private final Long userId;
    private final PersonalColorType personalColor;
}
