package com.threeboys.toneup.personalColor.infra;

import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeRequest;
import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeResponse;
import reactor.core.publisher.Mono;

import java.io.File;

public interface FastApiClient {
    PersonalColorAnalyzeResponse requestPersonalColorUpdate(PersonalColorAnalyzeRequest input);

    PersonalColorAnalyzeResponse requestPersonalColorUpdateWebClient(PersonalColorAnalyzeRequest input);

    PersonalColorAnalyzeResponse requestPersonalColorUpdateRestClient(PersonalColorAnalyzeRequest request);


    Mono<PersonalColorAnalyzeResponse> requestPersonalColorUpdateWebClientReactive(PersonalColorAnalyzeRequest request);

}
