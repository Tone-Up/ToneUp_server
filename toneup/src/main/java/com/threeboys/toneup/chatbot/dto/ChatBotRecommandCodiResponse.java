package com.threeboys.toneup.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotRecommandCodiResponse {
    private List<String> productPresignedUrlList;
    private List<String> productDetailHrefList;
}
