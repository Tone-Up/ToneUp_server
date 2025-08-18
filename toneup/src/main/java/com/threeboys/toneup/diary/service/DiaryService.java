package com.threeboys.toneup.diary.service;

import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.diary.domain.Diary;
import com.threeboys.toneup.diary.dto.*;
import com.threeboys.toneup.diary.exception.DiaryNotFoundException;
import com.threeboys.toneup.diary.repository.DiaryRepository;
import com.threeboys.toneup.feed.dto.FeedRequest;
import com.threeboys.toneup.feed.dto.FeedResponse;
import com.threeboys.toneup.feed.exception.FeedNotFoundException;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService;

    @Transactional
    public DiaryResponse createDiary(Long userId, DiaryRequest diaryRequest){
        UserEntity user = userRepository.getReferenceById(userId);
        String title = diaryRequest.getTitle();
        String content = diaryRequest.getContent();
        List<String> imageUrls = diaryRequest.getImageUrls();

        Diary diary  = new Diary(user, title, content);

        //save해서 feed id 받아오기(mysql)
        diaryRepository.save(diary);
        diary.attachImages(imageUrls);
        imageRepository.saveAll(diary.getImageUrlList());

        return new DiaryResponse(diary.getId());
    }

    @Transactional
    public void deleteDiary(Long userId, Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);
        diary.validateOwner(userId);

        //s3 기존 이미지 삭제 + images 테이블 삭제
        List<Images> imagesList = imageRepository.findByTypeAndRefId(ImageType.DIARY, diaryId);
        imagesList.forEach(images -> {fileService.deleteS3Object(images.getS3Key());});
        imageRepository.deleteAll(imagesList);

        diaryRepository.delete(diary);
    }

    @Transactional
    public DiaryDetailResponse getDiary(Long userId , Long diaryId) {
        //다중 조인으로 전체 조회(프로필, 다이어리 ,이미지들)
        List<DiaryDetailDto> diaryDetailDtoList = diaryRepository.findDiaryWithUserAndImage(diaryId, userId);
        // 이미지 s3Key로 s3 조회해서 url 획득 + 프로필 이미지도 획득
        List<String> imageUrls = diaryDetailDtoList.stream()
                .map(feedDetailDto -> fileService.getPreSignedUrl(feedDetailDto.getDiaryImageS3Key()))
                .toList();
        String profileImageUrl = fileService.getPreSignedUrl(diaryDetailDtoList.getFirst().getProfileS3Key());

        // groupBy로 묶고 dto에 넣어서 반환
        return DiaryDetailResponse.from(diaryDetailDtoList.getFirst(), profileImageUrl,imageUrls);
    }
    @Transactional
    public DiaryPageItemResponse getDiaryPreviews(Long userId , Long cursor, int limit) {
        //다중 조인으로 전체 조회(프로필, 피드 ,이미지들, 좋아요여부)
        DiaryPageItemResponse diaryPageItemResponse = diaryRepository.findDiaryPreviewsWithImage( userId, cursor, limit);
        // 이미지 s3Key로 s3 조회해서 url 획득 + 프로필 이미지도 획득
        diaryPageItemResponse.getDiaries().forEach(feedPreviewResponse -> {
            feedPreviewResponse.setImageUrl(fileService.getPreSignedUrl(feedPreviewResponse.getImageUrl()));
        });
        return diaryPageItemResponse;
    }
    @Transactional
    public DiaryResponse updateDiary(Long userId, Long diaryId, DiaryRequest diaryRequest) {
        Diary diary = diaryRepository.findById(diaryId).orElseThrow(DiaryNotFoundException::new);
        //작성자인지 검증
        diary.validateOwner(userId);

        //s3 기존 이미지 삭제 + images 테이블 삭제
        List<Images> imagesList = imageRepository.findByTypeAndRefId(ImageType.DIARY, diaryId);
        imagesList.forEach(images -> {fileService.deleteS3Object(images.getS3Key());});
        imageRepository.deleteAll(imagesList);



        diary.changeDiary(diaryRequest.getTitle(), diaryRequest.getContent(), diaryRequest.getImageUrls());
        imageRepository.saveAll(diary.getImageUrlList());

        return new DiaryResponse(diaryId);
    }
}
