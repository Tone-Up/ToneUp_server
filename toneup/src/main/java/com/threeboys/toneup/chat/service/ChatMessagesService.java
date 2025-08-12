package com.threeboys.toneup.chat.service;

import com.threeboys.toneup.chat.domain.ChatMessages;
import com.threeboys.toneup.chat.domain.ChatRoomUser;
import com.threeboys.toneup.chat.domain.ChatRooms;
import com.threeboys.toneup.chat.dto.*;
import com.threeboys.toneup.chat.exception.ChatRoomNotFoundException;
import com.threeboys.toneup.chat.repository.ChatMessagesRepository;
import com.threeboys.toneup.chat.repository.ChatRoomUserRepository;
import com.threeboys.toneup.chat.repository.ChatRoomsRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.socketio.dto.ChatMessage;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessagesService {
    private final ChatMessagesRepository chatMessagesRepository;
    private final ChatRoomsRepository chatRoomsRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserRepository userRepository;

    private final FileService fileService;

    @Transactional
    public void saveMessage(ChatMessage message, boolean isReceiverInRoom, int unreadCount) {
        Long roomId = Long.parseLong(message.getRoomId());
        ChatRooms chatRoom = chatRoomsRepository.findById(roomId).orElseThrow(ChatRoomNotFoundException::new);
        ChatMessages chatMessages = message.toEntity(chatRoom);

        //방에 상대방이 존재하는지 여부
        if(!isReceiverInRoom){
            chatMessages.updateUnreadCnt(unreadCount);
        }
        chatMessagesRepository.save(chatMessages);

        Long messageId = chatMessages.getId();
        String content = chatMessages.getContent();
        LocalDateTime sentAt = LocalDateTime.now();
        chatRoom.updateLastMessage(sentAt, content);
    }

    public Set<Long> getUserIdsInRoom(Long roomId) {
        return chatRoomUserRepository.findByChatRoomId(roomId).stream()
                .map(chatRoomUser -> chatRoomUser.getUser().getId())
                .collect(Collectors.toSet());
    }
    @Transactional
    public Long createChatRoom(CreateChatRoomRequest createChatRoomRequest){
        Long userId = createChatRoomRequest.getUserId();
        Long peerId = createChatRoomRequest.getPeerId();
        UserEntity user = userRepository.getReferenceById(userId);
        UserEntity peerUser = userRepository.getReferenceById(peerId);

        ChatRooms chatRooms = new ChatRooms();
        ChatRoomUser chatRoomUser = new ChatRoomUser(user, chatRooms);
        ChatRoomUser chatRoomPeerUser = new ChatRoomUser(peerUser, chatRooms);
        chatRoomsRepository.save(chatRooms);
        chatRoomUserRepository.save(chatRoomUser);
        chatRoomUserRepository.save(chatRoomPeerUser);

        return chatRooms.getId();
    }

    public void leaveChatRoom(Long chatRoomId, Long userId) {
        chatRoomUserRepository.deleteByChatRoomIdAndUserId(chatRoomId, userId);

    }

    public ChatPreviewResponse getChatList(Long userId, Long offset, int limit) {
        int page = (int) (offset / limit);
        Pageable pageable = PageRequest.of(page, limit, Sort.unsorted());

        Page<ChatPreviewDto> chatPage = chatRoomsRepository.findUserIdChatList(userId, pageable);

        // 프로필 이미지 URL 사전 서명 URL로 변환
        List<ChatPreviewDto> chatPreviewList = chatPage.stream()
                .map(dto -> {
                    dto.setPartnerProfileImageUrl(fileService.getPreSignedUrl(dto.getPartnerProfileImageUrl()));
                    return dto;
                })
                .collect(Collectors.toList());

        ChatPreviewResponse response = new ChatPreviewResponse(chatPreviewList);
        response.setHasNext(chatPage.hasNext());
        response.setNextCursor(offset + limit); // offset 기반 페이징일 경우
        response.setTotalCount(chatPage.getTotalElements());

        return response;
    }
    @Transactional
    public ChatDetailResponse getChatDetail(Long userId, Long chatRoomId, LocalDateTime lastSentAt) {
        List<ChatMessages> chatMessagesList = chatMessagesRepository.findByRoomIdAndSentAtGreaterThan(chatRoomId, lastSentAt);

        AtomicReference<Long> partnerId = null;
        ChatDetailResponse chatDetailResponse = new ChatDetailResponse();
        List<ChatDetailResponse.MessageDetailDto> messages = new ArrayList<>();
        chatMessagesList.stream().forEach(chatMessages -> {
            boolean isRead = chatMessages.getUnreadCount() == 0;
            boolean isMine = Objects.equals(userId, chatMessages.getSenderId());
            if(isMine) partnerId.set(chatMessages.getSenderId());

            messages.add(new ChatDetailResponse.MessageDetailDto(chatMessages.getId(), chatMessages.getSenderId(), chatMessages.getContent(), chatMessages.getSentAt(), isRead, isMine));
        });
        //unreadCount -1 더티 체킹으로
        chatMessagesList.stream().forEach(chatMessages -> {
            int unreadCount = chatMessages.getUnreadCount();
            if(unreadCount>0) chatMessages.updateUnreadCnt(unreadCount-1);
        });


        UserEntity partner = userRepository.findById(partnerId.get()).orElseThrow();
        chatDetailResponse.setMessages(messages);
        chatDetailResponse.setPartnerId(partner.getId());
        chatDetailResponse.setPartnerNickname(partner.getNickname());
        chatDetailResponse.setPartnerProfileImageUrl(fileService.getPreSignedUrl(partner.getProfileImageId().getS3Key()));


        return chatDetailResponse;
    }

//    public int userUnreadCount(Long userId){
//        return
//
//    }

}
