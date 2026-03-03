package com.threeboys.toneup.diary.service;

import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.exception.FORBIDDENException;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.diary.domain.Diary;
import com.threeboys.toneup.diary.dto.DiaryRequest;
import com.threeboys.toneup.diary.dto.DiaryResponse;
import com.threeboys.toneup.diary.exception.DiaryNotFoundException;
import com.threeboys.toneup.diary.repository.DiaryRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @InjectMocks
    private DiaryService diaryService;

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private FileService fileService;

    private UserEntity user;
    private Diary diary;
    private final Long USER_ID = 1L;
    private final Long DIARY_ID = 10L;

    @BeforeEach
    void setUp() {
        // 엔티티 초기화 로직 (테스트 픽스처)
        user = new UserEntity(USER_ID); 
        // Reflection을 통해 PK(id) 주입 - @GeneratedValue 된 ID를 테스트할 때 쓰는 정석적인 방법
        ReflectionTestUtils.setField(user, "id", USER_ID);

        diary = Diary.builder()
                .user(user)
                .title("오늘의 일기")
                .content("날씨가 좋다.")
                .build();
        ReflectionTestUtils.setField(diary, "id", DIARY_ID);
    }

    @Test
    @DisplayName("일기 생성: 성공 (입력값이 올바를 때 엔티티가 정상 생성 및 저장됨)")
    void createDiary_Success() {
        // Given
        List<String> imageUrls = Arrays.asList("url1.jpg", "url2.jpg");
        DiaryRequest request = new DiaryRequest("새로운 일기", "내용입니다", imageUrls);
        
        // Mocking: userId로 유저 엔티티를 찾아옴 (getReferenceById는 실제로 프록시지만 여기선 엔티티 리턴으로 모의)
        given(userRepository.getReferenceById(USER_ID)).willReturn(user);
        
        // diaryRepository.save(diary) 는 내부적으로 diary 객체의 상태만 변경(ID 할당 등)하므로 특별한 리턴 모킹 필요 X. 
        // 메서드가 save를 호출하는지만 뒤에서 verify로 검증.

        // When
        DiaryResponse response = diaryService.createDiary(USER_ID, request);

        // Then
        // 1. 동작 검증: Repository의 save 메서드들이 제대로 호출되었는지 확인
        verify(diaryRepository, times(1)).save(any(Diary.class));
        verify(imageRepository, times(1)).saveAll(anyList());

        // 2. 상태(결과) 검증: response 객체가 비어있지 않은지
        assertThat(response).isNotNull();
        // ID 주입은 실제 DB(JPA)가 해주지만, 테스트에서는 Mock이므로 ID가 null로 리턴되어도 객체 무결성 자체는 통과한 것임.
    }

    @Test
    @DisplayName("일기 수정: 성공 (작성자 본인이 요청하면 내용과 이미지가 변경됨)")
    void updateDiary_Success() {
        // Given
        List<String> oldImageUrls = Arrays.asList("old.jpg");
        diary.attachImages(oldImageUrls); // 기존 이미지 세팅

        List<String> newImages = Arrays.asList("new1.jpg", "new2.jpg");
        DiaryRequest updateRequest = new DiaryRequest("수정된 제목", "수정된 내용", newImages);

        given(diaryRepository.findById(DIARY_ID)).willReturn(Optional.of(diary));

        // 기존 삭제할 이미지 모킹
        List<Images> existingImages = diary.getImageUrlList();
        given(imageRepository.findByTypeAndRefId(ImageType.DIARY, DIARY_ID)).willReturn(existingImages);

        // When
        DiaryResponse response = diaryService.updateDiary(USER_ID, DIARY_ID, updateRequest);

        // Then
        // 다이어리 도메인 객체의 상태가 업데이트 되었는지 검증 (가장 중요한 비즈니스 로직 테스트)
        assertThat(diary.getTitle()).isEqualTo("수정된 제목");
        assertThat(diary.getContent()).isEqualTo("수정된 내용");
        assertThat(diary.getImageUrlList()).hasSize(2); // 새 이미지 2개로 덮어씌워짐

        // S3 삭제 및 DB 데이터 삭제 로직 호출 검증
        verify(fileService, times(1)).deleteS3Object(any());
        verify(imageRepository, times(1)).deleteAll(existingImages);
        verify(imageRepository, times(1)).saveAll(anyList()); // 새 이미지 저장 호출 검증
        
        assertThat(response.getDiaryId()).isEqualTo(DIARY_ID);
    }

    @Test
    @DisplayName("일기 수정: 실패 (작성자가 아닌 다른 유저가 수정을 요청할 경우 FORBIDDENException 발생)")
    void updateDiary_Fail_ByOtherUser() {
        // Given
        Long otherUserId = 999L; // 작성자가 아닌 제3의 유저 ID
        DiaryRequest updateRequest = new DiaryRequest("수정 시도", "내용 변경 시도", Arrays.asList("new.jpg"));

        given(diaryRepository.findById(DIARY_ID)).willReturn(Optional.of(diary));

        // When & Then
        // 권한이 없는 유저가 수정하려 할 때 예외가 정확히 터지고 상태변경이 안 일어남을 검증
        assertThatThrownBy(() -> diaryService.updateDiary(otherUserId, DIARY_ID, updateRequest))
                .isInstanceOf(FORBIDDENException.class);
        
        // 중요: 예외가 터졌으므로 그 아래의 삭제/수정/저장 로직은 '절대' 실행되면 안 됨!
        verify(imageRepository, never()).deleteAll(any());
        verify(imageRepository, never()).saveAll(any());
        verify(fileService, never()).deleteS3Object(any());
    }

    @Test
    @DisplayName("일기 단건 삭제: 실패 (존재하지 않는 다이어리 ID일 경우 DiaryNotFoundException 발생)")
    void deleteDiary_Fail_NotFound() {
        // Given
        given(diaryRepository.findById(DIARY_ID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> diaryService.deleteDiary(USER_ID, DIARY_ID))
                .isInstanceOf(DiaryNotFoundException.class);
    }

    @Test
    @DisplayName("일기 단건 조회: 정상 (작성자 프로필, 다이어리, 이미지 객체를 묶어서 DiaryDetailResponse 반환)")
    void getDiary_Success() {
        // Given
        com.threeboys.toneup.diary.dto.DiaryDetailDto dto = new com.threeboys.toneup.diary.dto.DiaryDetailDto(
                DIARY_ID, "조회 타이틀", "조회 내용", USER_ID, "닉네임", "profile_s3_key", "diary_s3_key"
        );
        List<com.threeboys.toneup.diary.dto.DiaryDetailDto> mockDtoList = Arrays.asList(dto, dto); // 동일 다이어리, 다중 이미지로 가정

        given(diaryRepository.findDiaryWithUserAndImage(DIARY_ID, USER_ID)).willReturn(mockDtoList);
        given(fileService.getPreSignedUrl("diary_s3_key")).willReturn("signed_diary_url");
        given(fileService.getPreSignedUrl("profile_s3_key")).willReturn("signed_profile_url");

        // When
        com.threeboys.toneup.diary.dto.DiaryDetailResponse response = diaryService.getDiary(USER_ID, DIARY_ID);

        // Then
        assertThat(response.getDiaryId()).isEqualTo(DIARY_ID);
        assertThat(response.getTitle()).isEqualTo("조회 타이틀");
        assertThat(response.getWriter().getNickname()).isEqualTo("닉네임");
        assertThat(response.getWriter().getProfileImageUrl()).isEqualTo("signed_profile_url");
        assertThat(response.getImageUrls()).containsExactly("signed_diary_url", "signed_diary_url"); // 맵핑된 결과 검증
    }

    @Test
    @DisplayName("일기 다건 페이징 조회: 정상 (커서 기반 페이지네이션 응답에 이미지 URL 가공 포함)")
    void getDiaryPreviews_Success() {
        // Given
        Long cursor = 0L;
        Integer limit = 10;
        
        com.threeboys.toneup.diary.dto.DiaryPreviewResponse preview1 = new com.threeboys.toneup.diary.dto.DiaryPreviewResponse(1L, "일기1", "s3_key_1");
        com.threeboys.toneup.diary.dto.DiaryPreviewResponse preview2 = new com.threeboys.toneup.diary.dto.DiaryPreviewResponse(2L, "일기2", "s3_key_2");
        com.threeboys.toneup.diary.dto.DiaryPageItemResponse mockPageResponse = new com.threeboys.toneup.diary.dto.DiaryPageItemResponse(
                Arrays.asList(preview1, preview2), 10L, true, 20L
        );

        given(diaryRepository.findDiaryPreviewsWithImage(USER_ID, cursor, limit)).willReturn(mockPageResponse);
        given(fileService.getPreSignedUrl("s3_key_1")).willReturn("signed_url_1");
        given(fileService.getPreSignedUrl("s3_key_2")).willReturn("signed_url_2");

        // When
        com.threeboys.toneup.diary.dto.DiaryPageItemResponse response = diaryService.getDiaryPreviews(USER_ID, cursor, limit);

        // Then
        assertThat(response.getDiaries()).hasSize(2);
        assertThat(response.getDiaries().get(0).getImageUrl()).isEqualTo("signed_url_1");
        assertThat(response.getDiaries().get(1).getImageUrl()).isEqualTo("signed_url_2");
        assertThat(response.getNextCursor()).isEqualTo(10L);
        assertThat(response.isHasNext()).isTrue();
    }
}
