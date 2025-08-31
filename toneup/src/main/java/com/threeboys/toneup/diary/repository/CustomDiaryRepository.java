package com.threeboys.toneup.diary.repository;

import com.threeboys.toneup.diary.dto.DiaryDetailDto;
import com.threeboys.toneup.diary.dto.DiaryPageItemResponse;

import java.util.List;

public interface CustomDiaryRepository {
    List<DiaryDetailDto> findDiaryWithUserAndImage(Long diaryId, Long userId);

    DiaryPageItemResponse findDiaryPreviewsWithImage(Long userId, Long cursor, Integer limit);
}
