package com.threeboys.toneup.personalColor.service;

import com.threeboys.toneup.personalColor.domain.PersonalColor;
import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeRequest;
import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeResponse;
import com.threeboys.toneup.personalColor.exception.PersonalColorNotFoundException;
import com.threeboys.toneup.personalColor.infra.FastApiClient;
import com.threeboys.toneup.personalColor.repository.PersonalColorRepository;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.exception.UserNotFoundException;
import com.threeboys.toneup.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalColorService {
    private final FastApiClient fastApiClient;
    private final UserRepository userRepository;
    private final PersonalColorRepository personalColorRepository;


//    @Transactional
    public String updatePersonalColor(Long userId, MultipartFile image) {
        long serviceStart = System.currentTimeMillis();
        log.info("[MARK-2] 서비스 진입 시간: {}", serviceStart);
        log.info("[MARK-2] 서비스 스레드: {}", Thread.currentThread());

//        long startTime = System.currentTimeMillis();
        // 1. 유저 조회
//        UserEntity userEntity = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException(userId));



        // 2. FastAPI 요청 객체 생성
        PersonalColorAnalyzeRequest request = new PersonalColorAnalyzeRequest(userId, image);


        // 3. FastAPI 결과 받아서 도메인 객체 생성
        PersonalColorAnalyzeResponse personalColor = fastApiClient.requestPersonalColorUpdate(request);
        log.info("[MARK-4] FastAPI 응답 완료 시점: {} ms", System.currentTimeMillis() - serviceStart);

        // 4. 퍼스널컬러 영속 엔티티 조회 (연관관계용)
//        PersonalColor colorEntity = personalColorRepository.findByPersonalColorType(personalColor.getPersonalColor())
//                .orElseThrow(() -> new PersonalColorNotFoundException(personalColor.getPersonalColor()));

        // 5. 유저에 퍼스널컬러 반영 (User 도메인에서 처리)
//        userEntity.updatePersonalColor(colorEntity);

        return personalColor.getPersonalColor().name();
    }
    @Transactional
    public String updatePersonalColorVirtualThread(Long userId, MultipartFile image) {
        // 1. 유저 조회
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. FastAPI 요청 객체 생성
        PersonalColorAnalyzeRequest request = new PersonalColorAnalyzeRequest(userEntity.getId(), image);

        // 3. FastAPI 호출을 가상 스레드로 비동기 처리
        PersonalColorAnalyzeResponse personalColor;

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<PersonalColorAnalyzeResponse> future = executor.submit(() ->
                    fastApiClient.requestPersonalColorUpdate(request)
            );
            personalColor = future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 중요!
            throw new RuntimeException("Thread was interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Exception during personal color analysis", e.getCause());
        }
        // 4. 퍼스널컬러 영속 엔티티 조회 (연관관계용)
        PersonalColor colorEntity = personalColorRepository.findByPersonalColorType(personalColor.getPersonalColor())
                .orElseThrow(() -> new PersonalColorNotFoundException(personalColor.getPersonalColor()));

        // 5. 유저에 퍼스널컬러 반영 (User 도메인에서 처리)
        userEntity.updatePersonalColor(colorEntity);
        return personalColor.getPersonalColor().name();
    }
//    @Transactional
    public String updatePersonalColorWebClient(Long userId, MultipartFile image) {
        // 1. 유저 조회
//        UserEntity userEntity = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. MultipartFile → 임시 파일로 저장

        PersonalColorAnalyzeRequest request = new PersonalColorAnalyzeRequest(userId, image);


        // 3. WebClient를 통한 FastAPI 호출 (.block() 사용)
        PersonalColorAnalyzeResponse response = fastApiClient.requestPersonalColorUpdateWebClient(request);


        // 4. 퍼스널컬러 엔티티 조회
//        PersonalColor colorEntity = personalColorRepository.findByPersonalColorType(response.getPersonalColor())
//                .orElseThrow(() -> new PersonalColorNotFoundException(response.getPersonalColor()));

        // 5. 유저 업데이트
//        userEntity.updatePersonalColor(colorEntity);
        return response.getPersonalColor().name();
    }
    public String updatePersonalColorRestClient(Long userId, MultipartFile image) {
        // 1. 유저 조회
//        UserEntity userEntity = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. MultipartFile → 임시 파일로 저장

        PersonalColorAnalyzeRequest request = new PersonalColorAnalyzeRequest(userId, image);


//부하테스트 피닝 발견위해
//                PersonalColorAnalyzeResponse response = fastApiClient.requestPersonalColorUpdate(request);
        PersonalColorAnalyzeResponse response = fastApiClient.requestPersonalColorUpdateRestClientGpt(request);


        // 3. WebClient를 통한 FastAPI 호출 (.block() 사용)

//        PersonalColorAnalyzeResponse response = fastApiClient.requestPersonalColorUpdateRestClientGpt(request);


        // 4. 퍼스널컬러 엔티티 조회
//        PersonalColor colorEntity = personalColorRepository.findByPersonalColorType(response.getPersonalColor())
//                .orElseThrow(() -> new PersonalColorNotFoundException(response.getPersonalColor()));

        // 5. 유저 업데이트
//        userEntity.updatePersonalColor(colorEntity);
        return response.getPersonalColor().name();
    }

    public Mono<String> updatePersonalColorWebClientFullReactive(Long userId, MultipartFile image) {
        // 1. 유저 조회
        Mono<UserEntity> userMono = Mono.fromCallable(() ->
                userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(userId))
        ).subscribeOn(Schedulers.boundedElastic());

        // 2. MultipartFile → PersonalColorAnalyzeRequest 생성
        Mono<PersonalColorAnalyzeRequest> requestMono = Mono.fromCallable(() ->
                new PersonalColorAnalyzeRequest(userId, image)
        ).subscribeOn(Schedulers.boundedElastic());

        // 전체 리액티브 흐름
        return userMono.zipWith(requestMono)
                .flatMap(tuple -> {
                    UserEntity userEntity = tuple.getT1();
                    PersonalColorAnalyzeRequest request = tuple.getT2();

                    // 3. WebClient 호출 (비동기)
                    return fastApiClient.requestPersonalColorUpdateWebClientReactive(request)
                            .flatMap(response -> {
                                // 4. 퍼스널컬러 조회
                                return Mono.fromCallable(() ->
                                                personalColorRepository.findByPersonalColorType(response.getPersonalColor())
                                                        .orElseThrow(() -> new PersonalColorNotFoundException(response.getPersonalColor()))
                                        ).subscribeOn(Schedulers.boundedElastic())
                                        .map(colorEntity -> {
                                            // 5. 유저 업데이트
                                            userEntity.updatePersonalColor(colorEntity);
                                            return response.getPersonalColor().name();
                                        });
                            });
                });
    }



}
