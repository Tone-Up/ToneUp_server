package com.threeboys.toneup.chatbot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class ChatBotRequest {
    private BotMessageType botMessageType;
    @Nullable
    private MultipartFile imageFile;       // 업로드된 이미지 파일
    @Nullable
    private String content;

    /**
     * MultipartFile을 Base64 문자열로 변환
     */
    public String getImageBase64() {
        try {
            if (imageFile == null || imageFile.isEmpty()) {
                return null;
            }
            byte[] bytes = imageFile.getBytes();
            return java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("이미지 Base64 변환 실패", e);
        }
    }
}
