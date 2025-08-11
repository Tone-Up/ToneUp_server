package com.threeboys.toneup.chat.service;

import com.threeboys.toneup.chat.domain.ChatMessages;
import com.threeboys.toneup.chat.domain.ChatRoomUser;
import com.threeboys.toneup.chat.domain.ChatRooms;
import com.threeboys.toneup.chat.dto.ChatDetailResponse;
import com.threeboys.toneup.chat.dto.ChatListRequest;
import com.threeboys.toneup.chat.dto.ChatPreviewResponse;
import com.threeboys.toneup.chat.dto.CreateChatRoomRequest;
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
import org.springframework.stereotype.Service;

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
        chatRoom.updateLastMessage(messageId, content);
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

    public ChatPreviewResponse getChatList(Long userId, ChatListRequest chatListRequest) {

        return chatRoomsRepository.findUserIdChatList(userId, chatListRequest);
    }

    public ChatDetailResponse getChatDetail(Long userId, Long chatRoomId, Long lastMessageId) {
        List<ChatMessages> chatMessagesList = chatMessagesRepository.findByRoomIdAndIdGreaterThan(chatRoomId, lastMessageId);

        AtomicReference<Long> partnerId = null;
        ChatDetailResponse chatDetailResponse = new ChatDetailResponse();
        List<ChatDetailResponse.MessageDetailDto> messages = new ArrayList<>();
        chatMessagesList.stream().forEach(chatMessages -> {
            boolean isRead = chatMessages.getUnreadCount() == 0;
            boolean isMine = Objects.equals(userId, chatMessages.getSenderId());
            if(isMine) partnerId.set(chatMessages.getSenderId());

            messages.add(new ChatDetailResponse.MessageDetailDto(chatMessages.getId(), chatMessages.getSenderId(), chatMessages.getContent(), chatMessages.getSentAt(), isRead, isMine));
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
