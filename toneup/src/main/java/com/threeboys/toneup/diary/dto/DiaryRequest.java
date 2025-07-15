package com.threeboys.toneup.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DiaryRequest {
    private String title;
    private String content;
    private List<String> imageUrls;
}
