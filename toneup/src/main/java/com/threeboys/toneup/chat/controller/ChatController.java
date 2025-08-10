package com.threeboys.toneup.chat.controller;

import com.threeboys.toneup.chat.dto.ChatListRequest;
import com.threeboys.toneup.chat.dto.ChatPreviewResponse;
import com.threeboys.toneup.chat.dto.CreateChatRoomRequest;
import com.threeboys.toneup.chat.service.ChatMessagesService;
import com.threeboys.toneup.common.request.FileNamesDTO;
import com.threeboys.toneup.common.response.PresignedUrlListResponseDTO;
import com.threeboys.toneup.common.response.PresignedUrlResponseDTO;
import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatMessagesService chatMessagesService;

    @PostMapping("/chats")
    public ResponseEntity<?> createChatRoom(@RequestBody CreateChatRoomRequest createChatRoomRequest){
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok", Map.of("roomId", chatMessagesService.createChatRoom(createChatRoomRequest))));
    }
    @DeleteMapping("/chats/{chatRoomId}")
    public ResponseEntity<?> leaveChatRoom(@PathVariable Long chatRoomId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        chatMessagesService.leaveChatRoom(chatRoomId, userId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok", Collections.emptyMap()));
    }

    @PostMapping("/chats")
    public ResponseEntity<?> getChatList(@RequestBody ChatListRequest chatListRequest , @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        ChatPreviewResponse chatPreviewResponse = chatMessagesService.getChatList(userId, chatListRequest);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok", chatPreviewResponse));

    }
    @GetMapping("/chats/{chatRoomId}")
    public ResponseEntity<?> getChatDetail(@PathVariable Long chatRoomId, @RequestParam(required = false) Long lastMessageId, @AuthenticationPrincipal CustomOAuth2User customOAuth2User){
        Long userId = customOAuth2User.getId();
        ChatPreviewResponse chatPreviewResponse = chatMessagesService.getChatDetail(userId, chatRoomId, lastMessageId);
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "ok", chatPreviewResponse));

    }

}
