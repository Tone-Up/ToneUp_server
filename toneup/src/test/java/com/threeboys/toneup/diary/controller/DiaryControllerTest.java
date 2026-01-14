package com.threeboys.toneup.diary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.threeboys.toneup.diary.dto.*;
import com.threeboys.toneup.diary.service.DiaryService;
import com.threeboys.toneup.security.CustomOAuth2User;
import com.threeboys.toneup.user.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiaryController.class)
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiaryService diaryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomOAuth2User customOAuth2User;

    private RequestPostProcessor oauthUser() {
        return oauth2Login().oauth2User(customOAuth2User);
    }

    @BeforeEach
    void setUp() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setRole("ROLE_USER");
        userDTO.setName("Test User");
        customOAuth2User = new CustomOAuth2User(userDTO);

        // // Security Context 설정 (OAuth2User 주입)
        // SecurityContextHolder.getContext().setAuthentication(
        // new UsernamePasswordAuthenticationToken(customOAuth2User, null,
        // customOAuth2User.getAuthorities()));
    }

    @Test
    @DisplayName("다이어리 생성 성공")
    void createDiary_Success() throws Exception {
        // given
        DiaryRequest request = new DiaryRequest("Title", "Content", List.of("img1.jpg"));
        DiaryResponse response = new DiaryResponse(100L);

        given(diaryService.createDiary(anyLong(), any(DiaryRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/my-diary")
                .with(csrf()) // POST 요청 시 CSRF 토큰 필요
                .with(oauthUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.diaryId").value(100L));
    }

    @Test
    @DisplayName("다이어리 상세 조회 성공")
    void getDiaryDetail_Success() throws Exception {
        // given
        Long diaryId = 100L;
        DiaryDetailDto diaryDetailDto = new DiaryDetailDto(
                diaryId, "Title", "Content", 1L, "Nickname", "profileKey", "img1.jpg");
        DiaryDetailResponse response = DiaryDetailResponse.from(diaryDetailDto, "ProfileUrl",
                List.of("img1.jpg", "img2.jpg"));

        given(diaryService.getDiary(anyLong(), eq(diaryId))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/my-diary/{diaryId}", diaryId)
                .with(oauthUser())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.diaryId").value(diaryId))
                .andExpect(jsonPath("$.data.title").value("Title"));
    }

    @Test
    @DisplayName("다이어리 목록 페이징 조회 성공")
    void getDiaryPagination_Success() throws Exception {
        // given
        DiaryPreviewResponse preview = new DiaryPreviewResponse(100L, "Title", "img.jpg");
        DiaryPageItemResponse response = new DiaryPageItemResponse(List.of(preview), 100L, true, 10L);

        given(diaryService.getDiaryPreviews(anyLong(), any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/my-diary")
                .param("cursor", "0")
                .param("limit", "10")
                .with(oauthUser())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.diaries[0].diaryId").value(100L));
    }

    @Test
    @DisplayName("다이어리 수정 성공")
    void updateDiary_Success() throws Exception {
        // given
        Long diaryId = 100L;
        DiaryRequest request = new DiaryRequest("Updated Title", "Updated Content", List.of("img_new.jpg"));
        DiaryResponse response = new DiaryResponse(diaryId);

        given(diaryService.updateDiary(anyLong(), eq(diaryId), any(DiaryRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/my-diary/{diaryId}", diaryId)
                .with(csrf())
                .with(oauthUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.diaryId").value(diaryId));
    }

    @Test
    @DisplayName("다이어리 삭제 성공")
    void deleteDiary_Success() throws Exception {
        // given
        Long diaryId = 100L;

        // when & then
        mockMvc.perform(delete("/api/my-diary/{diaryId}", diaryId)
                .with(csrf())
                .with(oauthUser())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
