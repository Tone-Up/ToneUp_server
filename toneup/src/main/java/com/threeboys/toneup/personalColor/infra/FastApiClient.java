package com.threeboys.toneup.personalColor.infra;

import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeRequest;
import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeResponse;
import com.threeboys.toneup.product.dto.ProductEmbeddingRequest;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;

public interface FastApiClient {
    PersonalColorAnalyzeResponse requestPersonalColorUpdate(PersonalColorAnalyzeRequest input);

    PersonalColorAnalyzeResponse requestPersonalColorUpdateWebClient(PersonalColorAnalyzeRequest input);

    PersonalColorAnalyzeResponse requestPersonalColorUpdateRestClient(PersonalColorAnalyzeRequest request);


    Mono<PersonalColorAnalyzeResponse> requestPersonalColorUpdateWebClientReactive(PersonalColorAnalyzeRequest request);

    Resource downloadEmbeddingFile(List<ProductEmbeddingRequest> request);

}
