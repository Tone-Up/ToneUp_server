//package com.threeboys.toneup.file;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.MediaType;
//import org.springframework.http.client.ClientHttpRequestFactory;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory; // 또는 JdkClient...
//import org.springframework.http.client.JdkClientHttpRequestFactory;
//import org.springframework.test.web.client.MockRestServiceServer;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestClient;
//
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
//import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
//
//public class DebuggingTest {
//
//    @Test
//    void captureTransferToCall() {
//        // 1. 범인으로 의심되는 팩토리 설정 (JDK Client 또는 Apache Client)
//         ClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(); // 피닝 유발자
////        ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(); // 해결사
//        // 2. RestClient 빌더 생성
//        RestClient.Builder builder = RestClient.builder().requestFactory(factory);
//
//        // 3. 가짜 서버(Mock) 연결 -> 실제 외부 통신 안 함! (타임아웃 걱정 끝)
//        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();
//        mockServer.expect(anything()).andRespond(withSuccess());
//
//        RestClient client = builder.build();
//
//        // 4. 문제의 Multipart 데이터 준비
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        // 👇 여기가 핵심! ByteArrayResource가 내부적으로 ByteArrayInputStream을 씁니다.
//        body.add("file", new ByteArrayResource(new byte[]{1, 2, 3, 4, 5}) {
//            @Override
//            public String getFilename() {
//                return "test.jpg";
//            }
//        });
//
//        // 5. 🚨 여기서 브레이크포인트를 걸고 실행하세요!
//        // (정확히는 ByteArrayInputStream.transferTo 메서드에 거세요)
//        client.post()
//                .uri("http://localhost/dummy") // 주소는 아무거나 상관없음
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .body(body)
//                .retrieve()
//                .toBodilessEntity();
//    }
//}
