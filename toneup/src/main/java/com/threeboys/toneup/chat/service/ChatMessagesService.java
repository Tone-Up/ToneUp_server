package com.threeboys.toneup.chat.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.threeboys.toneup.chat.domain.ChatMessages;
import com.threeboys.toneup.chat.domain.ChatRoomUser;
import com.threeboys.toneup.chat.domain.ChatRooms;
import com.threeboys.toneup.chat.domain.MessageType;
import com.threeboys.toneup.chat.dto.*;
import com.threeboys.toneup.chat.exception.ChatRoomNotFoundException;
import com.threeboys.toneup.chat.repository.ChatMessagesRepository;
import com.threeboys.toneup.chat.repository.ChatRoomUserRepository;
import com.threeboys.toneup.chat.repository.ChatRoomsRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.socketio.controller.RoomController;
import com.threeboys.toneup.socketio.dto.ChatMessage;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.UserNotFoundException;
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
import java.util.*;
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
    private final SocketIOServer server;


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
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        ChatMessage message = ChatMessage.create(userId, "유저 : "+ user.getNickname() +" 이 나갔습니다.");
        message.setType(MessageType.LEAVE);
        ChatRooms chatRoom = chatRoomsRepository.findById(chatRoomId).orElseThrow();
        chatMessagesRepository.save(message.toEntity(chatRoom));

        chatRoomUserRepository.deleteByChatRoomIdAndUserId(chatRoomId, userId);
        findUserAndLeaveRoom(userId, chatRoomId);
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

        List<ChatRoomUser> chatRoomUserList = chatRoomUserRepository.findByChatRoomId(chatRoomId);
        List<UserEntity> partner = chatRoomUserList.stream()
                .map(ChatRoomUser::getUser)
                .filter(user -> !user.getId().equals(userId)) // 내가 아닌 유저만
                .toList();

        ChatDetailResponse chatDetailResponse = new ChatDetailResponse();
        List<ChatDetailResponse.MessageDetailDto> messages = new ArrayList<>();

        //unreadCount -1 더티 체킹으로
        chatMessagesList.stream().forEach(chatMessages -> {
            int unreadCount = chatMessages.getUnreadCount();
            if(unreadCount>0&& !Objects.equals(chatMessages.getSenderId(), userId)) chatMessages.updateUnreadCnt(unreadCount-1);
        });

        for (ChatMessages value : chatMessagesList) {
            boolean isRead = (value.getUnreadCount() == 0);
            boolean isMine = Objects.equals(userId, value.getSenderId());
            messages.add(new ChatDetailResponse.MessageDetailDto(value.getId(), value.getSenderId(), value.getContent(), value.getSentAt(), isRead, isMine));
        }

        chatDetailResponse.setMessages(messages);
        chatDetailResponse.setPartnerId(partner.getFirst().getId());
        chatDetailResponse.setPartnerNickname(partner.getFirst().getNickname());
        chatDetailResponse.setPartnerProfileImageUrl(fileService.getPreSignedUrl(partner.getFirst().getProfileImageId().getS3Key()));

        return chatDetailResponse;
    }

    public void findUserAndLeaveRoom(Long userId, Long chatRoomId) {
        String roomId = chatRoomId.toString();
        Collection<SocketIOClient> clientCollection = server.getRoomOperations(roomId).getClients();
        SocketIOClient client = clientCollection.stream()
                .filter(socketIOClient -> {
                    String clientUserId = socketIOClient.getHandshakeData().getSingleUrlParam("userId");
                    return clientUserId != null && clientUserId.equals(String.valueOf(userId));
                })
                .findFirst().orElseThrow();
        client.leaveRoom(roomId);
        String nickname = client.getHandshakeData().getSingleUrlParam("nickname");
        server.getRoomOperations(roomId).sendEvent("leaveRoom", "유저 : " + nickname + " 이 영구적으로 나갔습니다.");

    }


}
