package com.threeboys.toneup.personalColor.controller;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.personalColor.dto.PersonalColorResponse;
import com.threeboys.toneup.personalColor.service.PersonalColorService;
import com.threeboys.toneup.security.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PersonalColorController {

    private final PersonalColorService personalColorService;

//    @PostMapping("/personalcolor")
//    public ResponseEntity<?> createPersonalColor(@RequestPart("image") MultipartFile imageFile, @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
//        Long userId = oAuth2User.getId();
//        String personalColor = personalColorService.updatePersonalColor(userId, imageFile);
//        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", personalColor));
//    }
    @PostMapping("/personalcolor")
    public ResponseEntity<?> createPersonalColorNotFastApi(@RequestPart("image") MultipartFile imageFile) {
        // Long userId = oAuth2User.getId();
//        String personalColor = personalColorService.updatePersonalColor(userId, imageFile);
        String personalColor = null;
        try{
            Thread.sleep(8000);
            personalColor = "AUTUM WARM";
        }catch (InterruptedException e){
            log.error("InterruptedException : "+ e.getMessage());
        }
        PersonalColorResponse personalColorResponse = new PersonalColorResponse(personalColor);

        return ResponseEntity.ok(new StandardResponse<>(true, 200, "Ok",personalColorResponse));
    }

    @PostMapping("/personalcolortest")
    public ResponseEntity<?> createPersonalColortest(@RequestPart("image") MultipartFile imageFile) {
        Long userId = 1L;

        long startTime = System.currentTimeMillis();
        log.info("[MARK-0] 컨트롤러 진입: {}", startTime);
        log.info("[MARK-0] 컨트롤러 스레드: {}", Thread.currentThread());

        String personalColor = personalColorService.updatePersonalColorRestClient(userId, imageFile);

        long endTime = System.currentTimeMillis();
        log.info("[MARK-1] personalColorService 응답 시간: {} ms", (endTime - startTime));
        log.info("[MARK-1] 컨트롤러 스레드: {}", Thread.currentThread());
        return ResponseEntity.ok(new StandardResponse<>(true, 0, "Ok", personalColor));
    }
    @PostMapping("/personalcolortestreact")
    public Mono<String> analyzeColor(@RequestPart("image") MultipartFile image) {
        Long userId = 1L;
        return personalColorService.updatePersonalColorWebClientFullReactive(userId, image);
    }
}


