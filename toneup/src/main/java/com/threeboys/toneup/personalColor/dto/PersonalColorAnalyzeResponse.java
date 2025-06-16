package com.threeboys.toneup.personalColor.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class PersonalColorAnalyzeResponse {
    private final Long userId;
    private final PersonalColorType personalColor;

    @JsonCreator
    public PersonalColorAnalyzeResponse(@JsonProperty("userId") Long userId,@JsonProperty("personalColor") String personalColor) {
        this.userId = userId;
        this.personalColor = PersonalColorType.valueOf(personalColor);
    }
}
