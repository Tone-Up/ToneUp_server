package com.threeboys.toneup.chat.service;

import com.threeboys.toneup.chat.domain.ChatMessages;
import com.threeboys.toneup.chat.domain.ChatRoomUser;
import com.threeboys.toneup.chat.domain.ChatRooms;
import com.threeboys.toneup.chat.dto.CreateChatRoomRequest;
import com.threeboys.toneup.chat.exception.ChatRoomNotFoundException;
import com.threeboys.toneup.chat.repository.ChatMessagesRepository;
import com.threeboys.toneup.chat.repository.ChatRoomUserRepository;
import com.threeboys.toneup.chat.repository.ChatRoomsRepository;
import com.threeboys.toneup.feed.exception.FeedNotFoundException;
import com.threeboys.toneup.socketio.dto.ChatMessage;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessagesService {
    private final ChatMessagesRepository chatMessagesRepository;
    private final ChatRoomsRepository chatRoomsRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserRepository userRepository;

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

//    public int userUnreadCount(Long userId){
//        return
//
//    }

}
