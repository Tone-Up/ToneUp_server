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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<ChatRoomUser> userChatRoom = chatRoomUserRepository.findByUserId(userId);
        List<ChatRoomUser> peerChatRoom = chatRoomUserRepository.findByUserId(peerId);

        Set<Long> userRoomIds = peerChatRoom.stream()
                .map(c -> c.getChatRoom().getId())
                .collect(Collectors.toSet());

        Set<Long> peerRoomIds = peerChatRoom.stream()
                .map(c -> c.getChatRoom().getId())
                .collect(Collectors.toSet());

        boolean hasCommonRoom = userChatRoom.stream()
                .map(c -> c.getChatRoom().getId())
                .anyMatch(peerRoomIds::contains);
        if(!hasCommonRoom){
            // 여기 들어오는 순간 현재 최소 두명 중 한명은 방에서 나간 상태

            // isActive = false 인 방에서 마지막 메시지를 가져와 나간 상대의 아이디가 리퀘스트의 상대방 아이디인 경우 그 방 리턴
            List<ChatRooms> userRoom = chatRoomsRepository.findByIsActiveFalseAndRoomsId(userRoomIds, peerId, PageRequest.of(0,1));
            //내 기준으로 상대방이 내 방에서 나간적이 있나
            if(userRoom.isEmpty()){
                //없다면 상대방 기준으로 내가 상대방 방에서 나간적이 있나
                List<ChatRooms> peerRoom = chatRoomsRepository.findByIsActiveFalseAndRoomsId(peerRoomIds, userId, PageRequest.of(0,1));
               if(!peerRoom.isEmpty()){
                   //상대방 방에 내가 다시 들어감
                   UserEntity user = userRepository.getReferenceById(peerId);
                   ChatRoomUser chatRoomPeerUser = new ChatRoomUser(user, peerRoom.getFirst());
                   chatRoomUserRepository.save(chatRoomPeerUser);
                   return peerRoom.getFirst().getId();
               }else{
                   //상대방 비활성화 방에도 유저 비활성화 방에서 존재 하지 않으므로 아예 새로 방 만들기
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

            }else{
                //상대방이 내 방에서 나갔으므로 내방으로 초대
                UserEntity peerUser = userRepository.getReferenceById(peerId);
                ChatRoomUser chatRoomPeerUser = new ChatRoomUser(peerUser, userRoom.getFirst());
                chatRoomUserRepository.save(chatRoomPeerUser);

                return userRoom.getFirst().getId();
            }

        }else{
            //둘 다 방에 있는 경우
            List<Long> commonRoomIds = userChatRoom.stream()
                    .map(c -> c.getChatRoom().getId())
                    .filter(peerRoomIds::contains)
                    .toList();
            return commonRoomIds.getFirst();
        }

    }
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        ChatMessage message = ChatMessage.create(userId, "유저 : "+ user.getId() +" 이 나갔습니다.");
        message.setType(MessageType.LEAVE);
        ChatRooms chatRoom = chatRoomsRepository.findById(chatRoomId).orElseThrow();
        long roomInUserCount = chatRoomUserRepository.findByChatRoomId(chatRoomId).size();

        if(roomInUserCount<=2) chatRoom.changeIsActive(false);

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
        if(lastSentAt==null) lastSentAt = LocalDateTime.parse("2010-01-11T00:00:00");
        List<ChatMessages> chatMessagesList = chatMessagesRepository.findByRoomIdAndSentAtGreaterThan(chatRoomId, lastSentAt);

        List<ChatRoomUser> chatRoomUserList = chatRoomUserRepository.findByChatRoomId(chatRoomId);
        List<UserEntity> partner = chatRoomUserList.stream()
                .map(ChatRoomUser::getUser)
                .filter(user -> !user.getId().equals(userId)) // 내가 아닌 유저만
                .toList();

        ChatDetailResponse chatDetailResponse = new ChatDetailResponse();
        List<ChatDetailResponse.MessageDetailDto> messages = new ArrayList<>();

        //unreadCount -1 더티 체킹으로 이거는 지금 1대1 채팅시에만 가능한 구조임 단체 채팅되는 순간 내가 이전에 읽은 적이 있는지 확인할 로직이 더 있어야함
        chatMessagesList.stream().forEach(chatMessages -> {
            int unreadCount = chatMessages.getUnreadCount();
            if(unreadCount>0&& !Objects.equals(chatMessages.getSenderId(), userId)) chatMessages.updateUnreadCnt(unreadCount-1);
        });

        for (ChatMessages value : chatMessagesList) {
            boolean isRead = (value.getUnreadCount() == 0);
            boolean isMine = Objects.equals(userId, value.getSenderId());
            messages.add(new ChatDetailResponse.MessageDetailDto(value.getId(), value.getSenderId(), value.getContent(), value.getSentAt(), isRead, isMine, value.getType()));
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

    @Transactional
    public boolean checkPeerInRoom(Long roomId) {
        ChatRooms chatrooms = chatRoomsRepository.findById(roomId).orElseThrow();
        boolean isPeerInRoom = chatrooms.isActive();
        if(!isPeerInRoom){
            chatrooms.changeIsActive(true);
            ChatMessages chatMessage = chatMessagesRepository.findFirstByRoomIdAndTypeOrderBySentAtDesc(roomId, MessageType.LEAVE);
            UserEntity peerUser = userRepository.getReferenceById(chatMessage.getSenderId());
//            System.out.println(peerUser.getId()  +"  "  + chatMessage.getContent());
            ChatRoomUser chatRoomPeerUser = new ChatRoomUser(peerUser, chatrooms);

            chatRoomUserRepository.save(chatRoomPeerUser);
        }
        return isPeerInRoom;
    }
}
