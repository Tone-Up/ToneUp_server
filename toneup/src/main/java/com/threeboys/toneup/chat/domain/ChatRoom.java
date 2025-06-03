package com.threeboys.toneup.chat.domain;

import com.threeboys.toneup.socketio.DTO.ChatMessage;
import org.hibernate.boot.model.naming.IllegalIdentifierException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatRoom {

    private final Set<Long> participantIds = new HashSet<>();

    private ChatRoom(Long creatorId) {
        this.participantIds.add(creatorId);
    }


    public static ChatRoom create(Long creatorId) {
        return new ChatRoom(creatorId);
    }


    public Set<Long> getParticipantIds() {
        return Collections.unmodifiableSet(participantIds);
    }

    public void addParticipant(Long paticipantId) {
        if(participantIds.contains(paticipantId)){
            throw new IllegalStateException("이미 참여중인 사용자입니다.");
        }
        participantIds.add(paticipantId);
    }

    public ChatMessage sendMessage(Long senderId, String content) {
        if(!participantIds.contains(senderId)){
            throw new IllegalStateException("참여자가 아니므로 메시지를 보낼 수 없습니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
        }
        return ChatMessage.create(senderId, content);
    }
}
