package com.threeboys.toneup.chat;

import com.threeboys.toneup.chat.domain.ChatRoom;
import com.threeboys.toneup.socketio.dto.ChatMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class chatRoomRoomTest {
    /***
     * 1. 채팅방 생성
     * 2. 참여자 추가
     *
     */

    @Test
    void createChatRoom(){
        Long creatorId = 1L;

        ChatRoom room = ChatRoom.create(creatorId);
        assertThat(room.getParticipantIds()).containsExactly(creatorId);
    }

    @Test
    void addParticipant(){
        Long creatorId = 1L;
        ChatRoom room = ChatRoom.create(creatorId);

        Long paticipantId = 2L;
        room.addParticipant(paticipantId);

        assertThat(room.getParticipantIds()).contains(paticipantId);
    }

    @Test
    void 참여자_중복_추가_시_예외발생() {
        Long creatorId = 1L;
        ChatRoom room = ChatRoom.create(creatorId);


        Long paticipantId = 2L;
        room.addParticipant(paticipantId);

        assertThrows(IllegalStateException.class, () -> {
            room.addParticipant(paticipantId);
        });
    }
    @Test
    void 채팅메시지_성공(){
        Long creatorId = 1L;
        Long senderId = 2L;

        ChatRoom room = ChatRoom.create(creatorId);
        room.addParticipant(senderId);

        String content = "테스트 중";
        ChatMessage message = room.sendMessage(senderId, content);
        assertEquals(message.getContent(),content);
        assertEquals(message.getSenderId(),senderId);
    }
    @Test
    void sendMessage_fail_notParticipant() {
        Long creatorId = 1L;
        Long nonParticipantId = 3L;
        ChatRoom chatRoom = ChatRoom.create(creatorId);

        String content = "Hi";

        assertThrows(IllegalStateException.class, () -> {
            chatRoom.sendMessage(nonParticipantId, content);
        });
    }

    @Test
    void sendMessage_fail_emptyContent() {
        Long creatorId = 1L;
        Long senderId = 2L;
        ChatRoom chatRoom = ChatRoom.create(creatorId);
        chatRoom.addParticipant(senderId);

        assertThrows(IllegalArgumentException.class, () -> {
            chatRoom.sendMessage(senderId, "");
        });
    }

}
