package com.threeboys.toneup.personalColor.infra;

import com.threeboys.toneup.personalColor.domain.PersonalColor;
import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeRequest;
import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RequiredArgsConstructor
public class FastApiClientImpl implements FastApiClient{
    private final RestTemplate restTemplate;
    private final String fastApiUrl;
    @Override
    public PersonalColorAnalyzeResponse requestPersonalColorUpdate(PersonalColorAnalyzeRequest input) {

        // FastAPI 호출 (POST 예시)
        ResponseEntity<PersonalColorAnalyzeResponse> response = restTemplate.postForEntity(
                fastApiUrl + "/analyze-color", input, PersonalColorAnalyzeResponse.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("FastAPI 호출 실패");
        }

        return response.getBody();
    }
}