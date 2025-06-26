package com.threeboys.toneup.chat;

import com.threeboys.toneup.chat.domain.ChatMessages;
import com.threeboys.toneup.chat.domain.ChatRooms;
import com.threeboys.toneup.chat.repository.ChatMessagesRepository;
import com.threeboys.toneup.chat.repository.ChatRoomsRepository;
import com.threeboys.toneup.chat.service.ChatMessagesService;
import com.threeboys.toneup.socketio.DTO.ChatMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class chatMessagesTest {

    @Mock
    private ChatMessagesRepository chatMessagesRepository;
    @Mock
    private ChatRoomsRepository chatRoomsRepository;


    @InjectMocks
    private ChatMessagesService chatMessagesService;


    @Test
    @DisplayName("saveMessageTest유저 2명, 상대방 방에 있을때")
    void saveMessageTest(){
        // given
        ChatMessage message = mock(ChatMessage.class);
        ChatRooms chatRoom = new ChatRooms();
        ChatMessages entity = new ChatMessages(); // 더미 객체

        when(message.getRoomId()).thenReturn(1L);
        when(chatRoomsRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(message.toEntity(chatRoom)).thenReturn(entity);

        // when
        chatMessagesService.saveMessage(message,true, 2);

        // then
        verify(chatMessagesRepository).save(entity);
    }
}
