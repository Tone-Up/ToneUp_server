package com.threeboys.toneup.chat.service;

import com.threeboys.toneup.chat.domain.ChatMessages;
import com.threeboys.toneup.chat.domain.ChatRooms;
import com.threeboys.toneup.chat.exception.ChatRoomNotFoundException;
import com.threeboys.toneup.chat.repository.ChatMessagesRepository;
import com.threeboys.toneup.chat.repository.ChatRoomsRepositoty;
import com.threeboys.toneup.socketio.DTO.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessagesService {
    private final ChatMessagesRepository chatMessagesRepository;
    private final ChatRoomsRepositoty chatRoomsRepository;

    public void saveMessage(ChatMessage message, boolean isReceiverInRoom, int roomSize) {
        Long roomId = message.getRoomId();
        ChatRooms chatRoom = chatRoomsRepository.findById(roomId).orElseThrow(()-> new ChatRoomNotFoundException(roomId));
        ChatMessages chatMessages = message.toEntity(chatRoom);
        //방에 상대방이 존재하는지 여부
        if(isReceiverInRoom){
            chatMessagesRepository.save(chatMessages);
        }else{
            chatMessages.updateUnreadCnt(roomSize-1);
            chatMessagesRepository.save(chatMessages);
        }
    }
}
