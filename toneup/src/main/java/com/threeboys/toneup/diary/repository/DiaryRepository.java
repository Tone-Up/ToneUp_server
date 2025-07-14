package com.threeboys.toneup.diary.repository;

import com.threeboys.toneup.diary.domain.Diary;
import com.threeboys.toneup.diary.dto.DiaryDetailDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> , CustomDiaryRepository{
}
