package com.threeboys.toneup.common.config;

import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeResponse;
import com.threeboys.toneup.recommand.service.RecommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class WarmUpRunner implements ApplicationListener<ApplicationReadyEvent> {
    private final RecommandService recommandService;
    private final RestClient restClient;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
//        restClient.get().uri("test");
        recommandService.getProductItemPagination(1L, PersonalColorType.SPRING,null,10);
        log.info("[WARM-UP] Startup warm-up completed. : " + event.getApplicationContext().toString());
    }

//    @Override
//    public boolean supportsAsyncExecution() {
//        return ApplicationListener.super.supportsAsyncExecution();
//    }
}
