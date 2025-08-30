package com.threeboys.toneup.chatbot.dto;

import lombok.Getter;

@Getter
public enum BotMessageType {
    READY("계속 질문하기"), QnA("Q&A"), RECOMMEND_CODI("AI 상품 추천"), EVALUATE_CODI("코디 평가");

    private final String typeName;

    BotMessageType(String typeName) {
            this.typeName = typeName;
    }
}
