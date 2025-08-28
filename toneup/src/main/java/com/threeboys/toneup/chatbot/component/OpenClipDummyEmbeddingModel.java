package com.threeboys.toneup.chatbot.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("openClipEmbeddingModel")
public class OpenClipDummyEmbeddingModel extends AbstractEmbeddingModel {
//    private final OpenClipEmbeddingOptions defaultOptions;
    private final @JsonProperty("dimensions") Integer dimensions = 1024;

    public Integer getDimensions() {
        return dimensions;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        float[] dummyVector = new float[dimensions];
        Embedding result = new Embedding(dummyVector,0);
        return new EmbeddingResponse(List.of(result));
    }

    @Override
    public float[] embed(Document document) {
        return new float[0];
    }

}
