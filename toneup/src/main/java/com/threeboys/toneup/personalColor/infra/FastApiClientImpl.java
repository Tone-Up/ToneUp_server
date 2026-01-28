package com.threeboys.toneup.personalColor.infra;

import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeRequest;
import com.threeboys.toneup.personalColor.dto.PersonalColorAnalyzeResponse;
import com.threeboys.toneup.product.dto.ProductEmbeddingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Semaphore;

@Slf4j
@RequiredArgsConstructor
public class FastApiClientImpl implements FastApiClient{
    private final RestTemplate restTemplate;
    private final String fastApiUrl;
    private final RedissonClient redissonClient;


    private static final String SEMAPHORE_KEY = "personalColor:semaphore";
    private static final int PERMIT_COUNT = 12;

    @Override
    public PersonalColorAnalyzeResponse requestPersonalColorUpdate(PersonalColorAnalyzeRequest input) {
        try {

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            MultipartFile imageFile = input.getImage();
            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };

            body.add("file", resource);
            body.add("user_id", input.getUserId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            long startTime = System.currentTimeMillis();

            ResponseEntity<PersonalColorAnalyzeResponse> response = restTemplate.postForEntity(
                    fastApiUrl + "/analyze-color",
                    requestEntity,
                    PersonalColorAnalyzeResponse.class
            );

            long endTime = System.currentTimeMillis();
            log.info("FastAPI 호출 및 응답 소요 시간: {} ms", (endTime - startTime));

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("FastAPI 호출 실패");
            }

            return response.getBody();

        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 처리 중 오류 발생", e);
        }
    }

    private final WebClient webClient;
    @Override
    public PersonalColorAnalyzeResponse requestPersonalColorUpdateWebClient(PersonalColorAnalyzeRequest input){

        try{
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            MultipartFile imageFile = input.getImage();
            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };

            body.add("file", resource);
            body.add("user_id", input.getUserId());

            long startTime = System.currentTimeMillis();

            PersonalColorAnalyzeResponse response = webClient.post()
                    .uri(fastApiUrl + "/analyze-color")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(PersonalColorAnalyzeResponse.class)
                    .block();
            long endTime = System.currentTimeMillis();
            log.info("FastAPI 호출 및 응답 소요 시간: {} ms", (endTime - startTime));
            return response;

        } catch (IOException e) {
        throw new RuntimeException("이미지 파일 처리 중 오류 발생", e);
        }



    }
    private final RestClient restClient;

    private static final Semaphore SEMAPHORE = new Semaphore(12); // 동시에 최대 5개 요청만 허용
//    @Override
//    public PersonalColorAnalyzeResponse requestPersonalColorUpdate(PersonalColorAnalyzeRequest input) {
//        try {
//            SEMAPHORE.acquire(); // 요청 전 세마포어 획득 (없으면 대기)
//
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//
//            MultipartFile imageFile = input.getImage();
//            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
//                @Override
//                public String getFilename() {
//                    return imageFile.getOriginalFilename();
//                }
//            };
//
//            body.add("file", resource);
//            body.add("user_id", input.getUserId());
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//            long startTime = System.currentTimeMillis();
//
//            ResponseEntity<PersonalColorAnalyzeResponse> response = restTemplate.postForEntity(
//                    fastApiUrl + "/analyze-color",
//                    requestEntity,
//                    PersonalColorAnalyzeResponse.class
//            );
//
//            long endTime = System.currentTimeMillis();
//            log.info("FastAPI 호출 및 응답 소요 시간: {} ms", (endTime - startTime));
//
//            if (response.getStatusCode() != HttpStatus.OK) {
//                throw new RuntimeException("FastAPI 호출 실패");
//            }
//
//            return response.getBody();
//
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException("이미지 파일 처리 중 오류 발생", e);
//        } finally {
//            SEMAPHORE.release(); // 꼭 반환!
//        }
//    }

    @Override
    public PersonalColorAnalyzeResponse requestPersonalColorUpdateRestClient(PersonalColorAnalyzeRequest input) {

        try {
            SEMAPHORE.acquire(); // 요청 전 세마포어 획득 (없으면 대기)

            MultipartFile imageFile = input.getImage();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            body.add("file", resource);
            body.add("user_id", input.getUserId());

            long startTime = System.currentTimeMillis();
//            Thread.sleep(50); // 요청 간 텀 (선택)


            PersonalColorAnalyzeResponse response = restClient.post()
                    .uri(fastApiUrl + "/analyze-color")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(PersonalColorAnalyzeResponse.class);

            long endTime = System.currentTimeMillis();
            log.info("FastAPI 호출 및 응답 소요 시간: {} ms", (endTime - startTime));

            return response;

        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 처리 중 오류 발생", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 스레드 인터럽트 유지
            throw new RuntimeException("스레드 sleep 또는 세마포어 획득 중단", e);
        } finally {
            SEMAPHORE.release(); // 꼭 반환!
        }
    }

    @Override
    public PersonalColorAnalyzeResponse requestPersonalColorUpdateRestClientGpt(PersonalColorAnalyzeRequest input) {
        RSemaphore semaphore = redissonClient.getSemaphore(SEMAPHORE_KEY);
        semaphore.trySetPermits(PERMIT_COUNT, Duration.ofSeconds(30));
        try {
            semaphore.tryAcquire(); // non blocking
//            semaphore.acquire(); // blocking 요청 전 세마포어 획득


            MultipartFile imageFile = input.getImage();
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            MultipartBodyBuilder builder = new MultipartBodyBuilder();

//            builder.part("user_id", String.valueOf(input.getUserId()));
//            builder.part("file", imageFile.getResource());
            body.add("user_id", String.valueOf(input.getUserId()));
            body.add("file", new FileSystemResource(convert(imageFile))); // 파일처럼 인식시키기



//            MultiValueMap<String, HttpEntity<?>> body = builder.build();

            System.out.println("id : " + input.getUserId());
//            System.out.println(body.getFirst("file").getBody() + " : " + body.getFirst("file").getHeaders());
            long startTime = System.currentTimeMillis();
//
//            body.add("user_id", String.valueOf(input.getUserId()));
//            body.add("file", imageFile.getResource());
            PersonalColorAnalyzeResponse response = restClient.post()
                    .uri(fastApiUrl + "/analyze-color")
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)  // MultiValueMap + HttpEntity 그대로 넣기
                    .retrieve()
                    .body(PersonalColorAnalyzeResponse.class);

            long endTime = System.currentTimeMillis();
            log.info("FastAPI 호출 및 응답 소요 시간: {} ms", (endTime - startTime));

            return response;
        }
//        catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("스레드 sleep 또는 세마포어 획득 중단", e);
//        }
        catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release(); // 꼭 반환
        }
    }



//    @Override
//    public PersonalColorAnalyzeResponse requestPersonalColorUpdateRestClient(PersonalColorAnalyzeRequest input) {
//
//        try {
//            MultipartFile imageFile = input.getImage();
//
//            // MultiValueMap<String, Object> 생성
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
//                @Override
//                public String getFilename() {
//                    return imageFile.getOriginalFilename();
//                }
//            };
//            body.add("file", resource);
//            body.add("user_id", input.getUserId());
//
//            long startTime = System.currentTimeMillis();
//            Thread.sleep(50);
//
//            PersonalColorAnalyzeResponse response = restClient.post()
//                    .uri(fastApiUrl + "/analyze-color")
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .body(body)
//                    .retrieve()
//                    .body(PersonalColorAnalyzeResponse.class);
//
//            long endTime = System.currentTimeMillis();
//            log.info("FastAPI 호출 및 응답 소요 시간: {} ms", (endTime - startTime));
//            return response;
//
//        } catch (IOException e) {
//            throw new RuntimeException("이미지 파일 처리 중 오류 발생", e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException("스레드 sleep 오류",  e);
//        }
//
//
//    }

    private File convert(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("upload-", file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
    }

    @Override
    public Mono<PersonalColorAnalyzeResponse> requestPersonalColorUpdateWebClientReactive(PersonalColorAnalyzeRequest request) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        // 이미지 파일 변환
        MultipartFile imageFile = request.getImage();
        try {
            builder.part("file", new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            }).contentType(MediaType.APPLICATION_OCTET_STREAM);
        } catch (IOException e) {
            return Mono.error(new RuntimeException("이미지 변환 실패", e));
        }

        // user_id 추가
        builder.part("user_id", request.getUserId().toString());

        // WebClient 리액티브 호출
        return webClient.post()
                .uri("/analyze-color")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(PersonalColorAnalyzeResponse.class);
    }

    @Override
    public Resource downloadEmbeddingFile(List<ProductEmbeddingRequest> request) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        return restClient.post()
                .uri(fastApiUrl+"/embed")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Resource.class);

    }

}