package com.threeboys.toneup.chatbot.controller;

import com.threeboys.toneup.chatbot.dto.ChatBotRequest;
import com.threeboys.toneup.chatbot.service.ChatbotService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping("/chatbot")
    public Object streamChat(@ModelAttribute ChatBotRequest chatbotRequest, @AuthenticationPrincipal CustomOAuth2User customOAuth2User) throws IOException {
        Long userId = customOAuth2User.getId();
        Object answer = chatbotService.generateStream(chatbotRequest, userId);
        return  answer;
    }
}
