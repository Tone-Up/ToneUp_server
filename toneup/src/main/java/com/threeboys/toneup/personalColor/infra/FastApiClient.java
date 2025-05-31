package com.threeboys.toneup.personalColor.infra;

import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeRequest;
import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeResponse;

public interface FastApiClient {
    PersonalColorAnalyzeResponse requestPersonalColorUpdate(PersonalColorAnalyzeRequest input);
}
