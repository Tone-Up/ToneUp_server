# ToneUp_server
# **프로젝트**
### 아키텍처
<img width="931" height="586" alt="Image" src="https://github.com/user-attachments/assets/f63197b1-159e-4d76-b48b-11cc1098b1df" />

### tone-up

프로젝트 기간/ 인원

2025.05. ~

FE 1명, BE 1명

프로젝트 내용

퍼스널 컬러 분석을 통한 개개인 맞춤형 상품 추천 및 sns 서비스 

### 외부 서버 호출 시 스레드 부하 문제 해결

- **상황**: 퍼스널 컬러 분석을 위해 FastAPI 기반의 외부 서버에 요청 필요
- **문제**: 서비스 부하가 증가할때 응답 속도가 현저히 떨어짐
- RestTemplate`을 사용할 경우 동기 방식으로 인해 스레드 점유가 심해지는걸로 잘못 판단하여 가상스레드, webclient 모두 다 사용해보았지만 해결되지 않음
- 첫번째 원인은 FastAPI측 멀티 프로세스 처리로 해결(--worker 수 증가)
- 이후 로그를 통해 파악한 결과 FastAPI 응답 시간과 스프링 컨트롤러->서비스 호출 시 응답 시간에 많은 차이가 발생(2배 이상)
- 컨트롤러로 들어오는 부분이 아니라 서비스 호출 시 발생되는 문제이므로 톰캣 스레드 풀 문제는 아니라고 판단
- 이미지 처리 부분, db 조회 부분 로그도 찍어보았지만 이상 없었음
- 결국 원인은 Hikari-pool(DBCP) 문제로 결론남 커넥션 풀 부족으로 인해 대기시간이 응답속도 저하 원인
- 이후  RestTemplate, Webclient 방식을 비교 RestTemplate, RestTemplate + 가상 스레드, Webclient, Webclient + 가상스레드 부하 테스트 결과
- 그냥 RestTemplate + 가상 스레드 방식이 제일 효율적이었음
  - 하지만 스레드 Pinning 현상이 발생
  - 그래서 가상스레드 + WebClient 방식 (.block()) 으로 결정(단 WebClient 특성 상 동시 요청으로 인해 FastApi에 병목이 발생하는 문제 해결위해 요청 속도 제한 설정 필요할 것으로 예상됨)
- #`RestTemplate`을 사용할 경우 동기 방식으로 인해 스레드 점유가 심해지는지 알았지만 다른 병목 지점 문제
-           스프링 자체 스레드 풀 문제로 파악중 FastAPI 병목은 확실히 아님
-           
- **행동**: `RestClient` +`VirtualThread`를 도입하면서 요청 속도 제한, 타임아웃 등을 적용하여 처리성능 개선
- **결과**: 전체 요청 처리 속도가 개선, 응답 편차 적어짐

  # 스프링 서버에서 외부 api 요청 시 스레드 부하 문제 해결(RestTemplate, WebClient, 가상 스레드)

### 퍼스널 컬러 분석 요청 시 블로킹 I/O 문제 해결

**상황**

FastAPI로 구축된 퍼스널 컬러 분석 서버와 Spring Boot 서버 간의 이미지 기반 분석 요청 연동이 필요했습니다. 초기에는 `RestTemplate`을 사용하여 동기 방식으로 통신을 처리했지만, 평균 분석 시간이 약 2초로 길어 **해당 요청을 처리하는 스레드가 블로킹 상태로 대기**하는 문제가 발생했습니다. 이로 인해 서비스의 스레드 풀 자원이 소모되며, 동시 요청 처리에 한계가 나타났습니다.

**문제 인식**

- `RestTemplate`은 blocking I/O 기반으로 동작하여 요청 중인 스레드가 응답을 받을 때까지 점유됨
- 분석 요청 시간 동안 스레드가 묶이면서 TPS 저하 및 응답 지연 발생
    - 10명이서 동시 요청 한 번씩만 보냈는데도 평균 12초 최대 20초 소요
        - 네트워크 병목은 아님
    - fastapi 서버는 2초 간격으로 요청을 잘 처리하는 것을 확인하여 병목 지점 제외
    - 톰캣 스레드 풀 의심
- 스레드 수 증가에 따라 CPU 컨텍스트 스위칭 비용 증가도 확인됨

**목표**

- 요청 응답 시간 편차가 최대한 적게(일관되게)
- 응답 시간을 짧게(10초 안으로)

- fastApi 워커 스레드 증가 4workers
    - 50%이상 대기시간 줄어듦


**해결 방향**

1. FastApi 멀티 프로세스를 이용해 처리량 증가

Java 21을 도입한 상황을 고려하여 아래 두 가지 방식을 비교 적용했습니다:

1. **RestTemplate + Virtual Thread (가상 스레드)**
    - `Executors.newVirtualThreadPerTaskExecutor()`를 활용해 요청을 가상 스레드에서 처리
    - 기존 코드를 크게 수정하지 않고도 동시성 향상 기대
    - 하지만 pinning 현상 발생
2. **WebClient (비동기 non-blocking 방식)**
    - Reactor 기반의 `WebClient`를 통해 분석 요청을 non-blocking하게 처리
    - 리액티브 체계를 도입하여 리소스 효율을 극대화
3. **RestClient + Virtual Thread (가상 스레드)**
    - WebFlux 의존성도 필요 없음
    - RestTemplate 을 webclient와 비슷한 방식의 코드 (추상화) 적용 ⇒ 코드 깔끔해짐
    - HttpClient 구현체인 JDK Client 사용시 pinning 현상 발생하지 않음

**벤치마크 및 결과**

- 동일한 트래픽(초당 20건 분석 요청) 시나리오로 세 방식 비교 테스트 진행

- **WebClient는** non-blocking으로 인해 동시 요청 수가 높아 FastApi 측에서 부하가 증가되어 제외
    - + 리액티브 체계에서 오는 러닝커브(응답 요청이 테스트 다 끝나고 한꺼번에 들어오거나 역압(백프레셔)조절 어려움)
- **RestTemplate + Virtual Thread (가상 스레드)이 성능이 아주 근소하게 더 좋음 하지만 pinning 현상이 발생**
- **그 다음으로 좋은 RestClient + Virtual Thread (가상 스레드) 방식으로 결정**
    - 단, 가상 스레드 다 보니 **WebClient** 방식과 비슷하게 동시 요청 수가 많아 질 때에 성능 저하가 발생되어 세마포어로 역압 제어

톰캣 스레드 풀을 줄일 수는 없으니 세마포어를 이용한 배압조절로 fastapi에 적절한 요청이 가도록 구현?(근데 RestTemplate도 동시 요청 수 조절 가능한데 설정에서?)

**결론**

적은 동시 요청 수 , 짧은 부하에서는 가상 스레드 x + resttemplate 방식이  다른 방식들과 비슷하거나 조금 더 빠른 수준이나 부하가 조금만 길어져도(50초→100초) 성능이 점점 저하됨(효율 안 좋음)

전체 요청 처리 시간 2배 이상 차이남 

하지만 가상 스레드 방식은 blocking을  non-blocking 방식과 유사 하게 처리 하는 부분에서 동시 요청 수가 늘어나 fastapi 서버에 순간적인 부하를 줘 응답 속도가 저하 될 때가 많음(일관되지 못함) 이 제어를 위해선 요청이 많이 들어와도 일정 주기로 fastapi에 요청을 보내어 부하를 제어가 필요(백프레셔 제어) 이 제어를 위해 세마포어로 가상스레드 수 제어 및 sleep으로 요청 주기 제어 필요할듯

**개선점**

이 방식은 FastApi의 동시 처리 가능 요청 수를 테스트해보고 그 결과에 따라 스프링과 FastApi를 제한한 방식으로  FastApi가 스스로의 부하에 따라 요청을 가져와서 처리하는 방식과는 차이가 있어서 추후에 카프카나 rabbitMQ를 이용한 메시지 큐 방식으로 처리하면 성능 향상이 더 있지 않을까 싶다. 아니면 이 방식은 그대로 유지 하되 메시지 큐에 결과 데이터를 넣어서 비동기 방식으로 처리하거나(현재는 FastApi의 응답 결과 대기가 너무 길어져서 트랜잭션을 최대한 쪼개 DBCP 효율 증가 하는 방식임).

### 가상스레드 + restclient

```
default ↓ [ 100% ] 20 VUs  50s
 ✓ status is 200

 checks.........................: 100.00% ✓ 164      ✗ 0
 data_received..................: 60 kB   1.1 kB/s
 data_sent......................: 82 MB   1.5 MB/s
 http_req_blocked...............: avg=1.2ms    min=3.84µs   med=6.09µs   max=15.08ms  p(90)=4.75ms  p(95)=12.07ms
 http_req_connecting............: avg=1ms      min=0s       med=0s       max=14.91ms  p(90)=3.4ms   p(95)=11.3ms
 http_req_duration..............: avg=5.42s    min=3.2s     med=4.32s    max=11.43s   p(90)=9.44s   p(95)=10s
   { expected_response:true }...: avg=5.42s    min=3.2s     med=4.32s    max=11.43s   p(90)=9.44s   p(95)=10s
 http_req_failed................: 0.00%   ✓ 0        ✗ 164
 http_req_receiving.............: avg=651.19µs min=101.24µs med=168.61µs max=22.37ms  p(90)=1.54ms  p(95)=1.78ms
 http_req_sending...............: avg=11.76ms  min=303.29µs med=395.67µs max=241.15ms p(90)=39.36ms p(95)=68.44ms
 http_req_tls_handshaking.......: avg=0s       min=0s       med=0s       max=0s       p(90)=0s      p(95)=0s
 http_req_waiting...............: avg=5.41s    min=3.2s     med=4.31s    max=11.43s   p(90)=9.44s   p(95)=9.98s
 http_reqs......................: 164     2.945107/s
 iteration_duration.............: avg=6.42s    min=4.2s     med=5.33s    max=12.43s   p(90)=10.44s  p(95)=11.01s
 iterations.....................: 164     2.945107/s
 vus............................: 2       min=2      max=20
 vus_max........................: 20      min=20     max=20
 running (0m55.7s), 00/20 VUs, 164 complete and 0 interrupted iterations
default ✓ [ 100% ] 20 VUs  50s

running (1m46.0s), 02/20 VUs, 286 complete and 0 interrupted iterations
default ↓ [ 100% ] 20 VUs  1m40s

     ✗ status is 200
      ↳  98% — ✓ 283 / ✗ 5

     checks.........................: 98.26% ✓ 283      ✗ 5
     data_received..................: 103 kB 969 B/s
     data_sent......................: 145 MB 1.4 MB/s
     http_req_blocked...............: avg=655.9µs  min=4µs      med=6.28µs   max=12.67ms p(90)=27.12µs p(95)=5.72ms
     http_req_connecting............: avg=388.04µs min=0s       med=0s       max=6.31ms  p(90)=0s      p(95)=3.65ms
     http_req_duration..............: avg=6.17s    min=2.99s    med=4.21s    max=59.99s  p(90)=8.95s   p(95)=10.12s
       { expected_response:true }...: avg=5.22s    min=2.99s    med=4.19s    max=14.71s  p(90)=8.8s    p(95)=9.71s
     http_req_failed................: 1.73%  ✓ 5        ✗ 283
     http_req_receiving.............: avg=2.04ms   min=0s       med=1.37ms   max=33.48ms p(90)=2.98ms  p(95)=5.7ms
     http_req_sending...............: avg=5.4ms    min=263.14µs med=389.04µs max=228ms   p(90)=18.83ms p(95)=29.65ms
     http_req_tls_handshaking.......: avg=0s       min=0s       med=0s       max=0s      p(90)=0s      p(95)=0s
     http_req_waiting...............: avg=6.17s    min=2.99s    med=4.21s    max=59.97s  p(90)=8.93s   p(95)=10.12s
     http_reqs......................: 288    2.701965/s
     iteration_duration.............: avg=7.18s    min=4s       med=5.22s    max=1m1s    p(90)=9.95s   p(95)=11.12s
     iterations.....................: 288    2.701965/s
     vus............................: 2      min=2      max=20
     vus_max........................: 20     min=20     max=20

running (1m46.6s), 00/20 VUs, 288 complete and 0 interrupted iterations
default ✓ [ 100% ] 20 VUs  1m40s

sleep(30)
running (1m45.0s), 01/20 VUs, 299 complete and 0 interrupted iterations
default ↓ [ 100% ] 20 VUs  1m40s

     ✓ status is 200

     checks.........................: 100.00% ✓ 300      ✗ 0
     data_received..................: 110 kB  1.0 kB/s
     data_sent......................: 151 MB  1.4 MB/s
     http_req_blocked...............: avg=470.39µs min=4.62µs   med=6.38µs   max=13.15ms  p(90)=9.75µs p(95)=4.61ms
     http_req_connecting............: avg=297.22µs min=0s       med=0s       max=13.03ms  p(90)=0s     p(95)=2.81ms
     http_req_duration..............: avg=5.84s    min=3.4s     med=4.88s    max=11.72s   p(90)=10.06s p(95)=10.58s
       { expected_response:true }...: avg=5.84s    min=3.4s     med=4.88s    max=11.72s   p(90)=10.06s p(95)=10.58s
     http_req_failed................: 0.00%   ✓ 0        ✗ 300
     http_req_receiving.............: avg=2ms      min=51.22µs  med=498.22µs max=31.17ms  p(90)=4.85ms p(95)=10.3ms
     http_req_sending...............: avg=7.79ms   min=286.88µs med=387.6µs  max=265.56ms p(90)=28.9ms p(95)=51.17ms
     http_req_tls_handshaking.......: avg=0s       min=0s       med=0s       max=0s       p(90)=0s     p(95)=0s
     http_req_waiting...............: avg=5.83s    min=3.4s     med=4.87s    max=11.69s   p(90)=10.05s p(95)=10.57s
     http_reqs......................: 300     2.855138/s
     iteration_duration.............: avg=6.84s    min=4.4s     med=5.88s    max=12.73s   p(90)=11.06s p(95)=11.58s
     iterations.....................: 300     2.855138/s
     vus............................: 1       min=1      max=20
     vus_max........................: 20      min=20     max=20

running (1m45.1s), 00/20 VUs, 300 complete and 0 interrupted iterations
default ✓ [ 100% ] 20 VUs  1m40s

세마포어 15
running (1m45.0s), 04/20 VUs, 298 complete and 0 interrupted iterations
default ↓ [ 100% ] 20 VUs  1m40s

     ✓ status is 200

     checks.........................: 100.00% ✓ 302      ✗ 0
     data_received..................: 110 kB  1.0 kB/s
     data_sent......................: 152 MB  1.4 MB/s
     http_req_blocked...............: avg=466.07µs min=4.03µs   med=6.56µs   max=13.03ms  p(90)=10.14µs p(95)=4.3ms
     http_req_connecting............: avg=352.62µs min=0s       med=0s       max=7.9ms    p(90)=0s      p(95)=4.15ms
     http_req_duration..............: avg=5.81s    min=3.17s    med=5.27s    max=15.09s   p(90)=8.33s   p(95)=10.8s
       { expected_response:true }...: avg=5.81s    min=3.17s    med=5.27s    max=15.09s   p(90)=8.33s   p(95)=10.8s
     http_req_failed................: 0.00%   ✓ 0        ✗ 302
     http_req_receiving.............: avg=1.13ms   min=63.66µs  med=182.6µs  max=15.83ms  p(90)=2.33ms  p(95)=4.05ms
     http_req_sending...............: avg=6.22ms   min=276.31µs med=376.85µs max=194.35ms p(90)=21.21ms p(95)=35.33ms
     http_req_tls_handshaking.......: avg=0s       min=0s       med=0s       max=0s       p(90)=0s      p(95)=0s
     http_req_waiting...............: avg=5.8s     min=3.16s    med=5.27s    max=15.04s   p(90)=8.33s   p(95)=10.79s
     http_reqs......................: 302     2.861868/s
     iteration_duration.............: avg=6.81s    min=4.17s    med=6.27s    max=16.09s   p(90)=9.33s   p(95)=11.8s
     iterations.....................: 302     2.861868/s
     vus............................: 4       min=4      max=20
     vus_max........................: 20      min=20     max=20

running (1m45.5s), 00/20 VUs, 302 complete and 0 interrupted iterations
default ✓ [ 100% ] 20 VUs  1m40s

세마포어12 fastapi에서도 요청 수 12 조절

running (1m45.0s), 04/20 VUs, 287 complete and 0 interrupted iterations
default ↓ [ 100% ] 20 VUs  1m40s

     ✓ status is 200

     checks.........................: 100.00% ✓ 291      ✗ 0
     data_received..................: 106 kB  1.0 kB/s
     data_sent......................: 146 MB  1.4 MB/s
     http_req_blocked...............: avg=543.34µs min=4.79µs     med=6.31µs   max=18.24ms  p(90)=9.64µs p(95)=5.06ms
     http_req_connecting............: avg=395.86µs min=0s         med=0s       max=18.08ms  p(90)=0s     p(95)=3.7ms
     http_req_duration..............: avg=6.09s    min=3.16s      med=5.66s    max=12.97s   p(90)=8.21s  p(95)=8.66s
       { expected_response:true }...: avg=6.09s    min=3.16s      med=5.66s    max=12.97s   p(90)=8.21s  p(95)=8.66s
     http_req_failed................: 0.00%   ✓ 0        ✗ 291
     http_req_receiving.............: avg=1.28ms   min=53.93µs    med=1.24ms   max=10.07ms  p(90)=2.37ms p(95)=3.14ms
     http_req_sending...............: avg=8.48ms   min=-1494208ns med=375.17µs max=159.99ms p(90)=28.8ms p(95)=55.15ms
     http_req_tls_handshaking.......: avg=0s       min=0s         med=0s       max=0s       p(90)=0s     p(95)=0s
     http_req_waiting...............: avg=6.08s    min=3.16s      med=5.66s    max=12.91s   p(90)=8.16s  p(95)=8.66s
     http_reqs......................: 291     2.751003/s
     iteration_duration.............: avg=7.09s    min=4.16s      med=6.66s    max=13.99s   p(90)=9.22s  p(95)=9.66s
     iterations.....................: 291     2.751003/s
     vus............................: 4       min=4      max=20
     vus_max........................: 20      min=20     max=20

running (1m45.8s), 00/20 VUs, 291 complete and 0 interrupted iterations
default ✓ [ 100% ] 20 VUs  1m40s

```

### 가상 스레드 x + resttemplate

```
 ✓ status is 200

 checks.........................: 100.00% ✓ 308     ✗ 0
 data_received..................: 112 kB  1.1 kB/s
 data_sent......................: 155 MB  1.5 MB/s
 http_req_blocked...............: avg=511.03µs min=4.29µs   med=6.42µs   max=11.14ms  p(90)=9.4µs   p(95)=5.29ms
 http_req_connecting............: avg=227.36µs min=0s       med=0s       max=4.7ms    p(90)=0s      p(95)=2.7ms
 http_req_duration..............: avg=5.69s    min=2.97s    med=4.14s    max=16.4s    p(90)=10.08s  p(95)=12.84s
   { expected_response:true }...: avg=5.69s    min=2.97s    med=4.14s    max=16.4s    p(90)=10.08s  p(95)=12.84s
 http_req_failed................: 0.00%   ✓ 0       ✗ 308
 http_req_receiving.............: avg=952.88µs min=44.71µs  med=204.72µs max=28.68ms  p(90)=1.82ms  p(95)=2.44ms
 http_req_sending...............: avg=5.39ms   min=256.86µs med=377.78µs max=197.74ms p(90)=11.58ms p(95)=30.63ms
 http_req_tls_handshaking.......: avg=0s       min=0s       med=0s       max=0s       p(90)=0s      p(95)=0s
 http_req_waiting...............: avg=5.68s    min=2.96s    med=4.14s    max=16.4s    p(90)=10.08s  p(95)=12.83s
 http_reqs......................: 308     2.90648/s
 iteration_duration.............: avg=6.69s    min=3.98s    med=5.14s    max=17.4s    p(90)=11.08s  p(95)=13.84s
 iterations.....................: 308     2.90648/s
 vus............................: 4       min=4     max=20
 vus_max........................: 20      min=20    max=20
running (1m46.0s), 00/20 VUs, 308 complete and 0 interrupted iterations
default ✓ [ 100% ] 20 VUs  1m40s

가상 스레드 x + resttemplate + 세마포어 12
running (1m49.0s), 03/20 VUs, 232 complete and 0 interrupted iterations
default ↓ [ 100% ] 20 VUs  1m40s

     ✓ status is 200

     checks.........................: 100.00% ✓ 235      ✗ 0
     data_received..................: 86 kB   781 B/s
     data_sent......................: 118 MB  1.1 MB/s
     http_req_blocked...............: avg=771.84µs min=3.87µs   med=6.09µs   max=12.53ms  p(90)=11.95µs p(95)=8.51ms
     http_req_connecting............: avg=315.35µs min=0s       med=0s       max=4.67ms   p(90)=0s      p(95)=3.02ms
     http_req_duration..............: avg=7.85s    min=3.75s    med=6.98s    max=15.41s   p(90)=12s     p(95)=13.12s
       { expected_response:true }...: avg=7.85s    min=3.75s    med=6.98s    max=15.41s   p(90)=12s     p(95)=13.12s
     http_req_failed................: 0.00%   ✓ 0        ✗ 235
     http_req_receiving.............: avg=1.63ms   min=93.49µs  med=1.41ms   max=9.78ms   p(90)=2.5ms   p(95)=3.22ms
     http_req_sending...............: avg=7.63ms   min=270.11µs med=390.52µs max=204.53ms p(90)=15.66ms p(95)=37.82ms
     http_req_tls_handshaking.......: avg=0s       min=0s       med=0s       max=0s       p(90)=0s      p(95)=0s
     http_req_waiting...............: avg=7.84s    min=3.67s    med=6.98s    max=15.41s   p(90)=11.99s  p(95)=13.11s
     http_reqs......................: 235     2.139202/s
     iteration_duration.............: avg=8.85s    min=4.77s    med=7.98s    max=16.42s   p(90)=13.01s  p(95)=14.12s
     iterations.....................: 235     2.139202/s
     vus............................: 3       min=3      max=20
     vus_max........................: 20      min=20     max=20

running (1m49.9s), 00/20 VUs, 235 complete and 0 interrupted iterations
default ✓ [ 100% ] 20 VUs  1m40s

가상 스레드 x + resttemplate 설정 커넥션 12개 제한

```

이 경험을 통해 Java 21의 가상 스레드 활용 가능성과 한계를 체감했으며, 비동기 방식과 비교해볼 수 있는 기준을 확보했습니다. 향후 고부하 I/O 처리 시 아키텍처 선택 기준을 명확히 할 수 있는 기반이 되었습니다.

---

### 첫 요청 딜레이 해결

- **상황**: 서비스 배포 후 첫 요청에서 응답 속도가 비정상적으로 느려지는 문제 존재
- **문제**: JVM 초기 구동 시 JIT 컴파일과 클래스 로딩 등의 이슈로 웜업 시간이 필요.
- **행동**: 배포 후 내부에서 미리 더미 요청을 보내는 웜업 로직을 추가
- **결과**: 사용자 기준 첫 요청 응답 속도가 정상화

---

### 유지보수성과 확장성을 고려한 인증 시스템 설계

- **상황**: 소셜 로그인 기능을 구현해야 했고, 단순히 Google만이 아닌 다른 플랫폼도 확장 고려가 필요
- **문제**: 추후 유지보수 및 확장을 고려한 설계가 요구됨
- **행동**: 전략 패턴, 템플릿 메서드 패턴, 팩토리 패턴을 적용하여 OOP 원칙에 맞게 인증 로직을 분리
- **결과**: 새로운 플랫폼 추가 시에도 기존 코드를 건드리지 않고 기능을 확장할 수 있는 구조를 완성

---

### 좋아요 기능의 동시성 문제 대응

- **상황**: 다수의 사용자가 동시에 좋아요를 누를 수 있는 기능이 필요
- **문제**: 동시 요청 처리 시 Race Condition으로 인해 데이터 정합성 문제
- **행동**: 분산 락(Mutex)을 적용하고, 실패 시 재시도 로직을 도입하여 안정적인 처리 흐름을 보장
- **결과**: 동시성 이슈 없이 일관성 있는 데이터가 유지

---
### 전문 검색 쿼리 

- **상황**: 상품이나 피드 조회에 대한 검색 결과 필요
- **문제**: MySQL에서 LIKE %검색어% 사용 시 인덱스를 타지 못해 전체 테이블을 스캔함, 연관성 기반 순으로 정렬된 검색 결과 필요함
- **행동**: Redis의 검색 모듈인 RediSearch를 도입
- **결과**: RediSearch 도입을 통해 검색 응답 시간을 단축하여, 이전 대비 **성능을 3배 향상(응답 시간 70% 감소)**
---

### 실시간 채팅 서버 구현

- **상황**: 사용자 간 실시간 채팅 기능이 요구
- **문제**: 안정적인 연결과 이벤트 기반 처리가 필요함
- **행동**: `Netty-SocketIO` 기반 서버를 구축하고, 커스텀 어노테이션을 통해 이벤트 핸들링 구조를 설계
- **결과**: 안정적인 실시간 채팅 기능을 완성하여 사용자 경험을 강화

---
### 실시간 채팅 리스트 쿼리 최적화

- **상황**: 1:1 및 단체 채팅방 목록에서 안 읽은 메시지 수(Unread Count)를 포함한 채팅 미리보기 데이터를 조회해야 하는 상황이었습니다.
- **문제**: 기존의 GROUP BY와 전체 메시지 COUNT 방식은 데이터가 쌓일수록 메시지 테이블 전체를 스캔(Full Scan)하여 성능이 급격히 저하되는 구조였습니다.
- **행동**: 사용자가 마지막으로 읽은 지점을 추적하는 lastReadMessageId 컬럼을 추가하고, 이를 활용해 인덱스 기반의 범위 카운트(Range Scan) 서브쿼리 방식으로 로직을 변경했습니다.(**예정**)
- **결과**: 메시지 수와 관계없이 조회 성능이 일정하게 유지되는 확장성을 확보했으며, 실행 계획상 ALL 스캔을 제거하여 DB 부하를 최소화했습니다.(단순 집계(COUNT) 방식의 한계를 인덱스 커서(Cursor) 기반의 시퀀스 트래킹 방식으로 해결하여, 대규모 단체 채팅 환경에서도 성능 저하 없는 목록 조회 기능을 구현함.)

---
### 저사양 환경에서의 대용량 JSON 스트리밍 및 Redis 인덱싱 최적화
- **상황**: 서비스 초기 구동 시, 약 911MB 규모의 상품 임베딩 및 자동완성 데이터를 외부 JSON 파일로부터 읽어 Redis Search에 캐싱해야 하는 상황이었습니다.

- **문제**: EC2 프리티어(RAM 1GB) 환경에서 Files.readString()이나 일반적인 ObjectMapper.readValue() 방식을 사용할 경우, 파일 전체가 메모리에 적재되어 **OutOfMemoryError(OOM)**가 발생하고 시스템이 중단되는 물리적 한계가 있었습니다.

- **행동**: Streaming Parser 도입: 파일 전체를 메모리에 올리지 않고 데이터를 한 입씩 읽어 처리하는 Jackson JsonParser 기반의 스트리밍 방식을 채택하여 메모리 점유율을 최소화했습니다.

Redis Pipelining & Batch 처리: 데이터를 건건이 저장하는 대신, 500개 단위의 배치(Batch)로 묶어 비동기 커맨드로 전송하는 파이프라이닝 기법을 적용해 네트워크 왕복 시간(RTT)을 획기적으로 줄였습니다.

OS 스왑 메모리 활용: 물리적 RAM 부족에 대비해 2GB의 Swap File을 설정하여 프로세스의 안정적인 완주를 보장했습니다.

- **결과**: 911MB의 대용량 데이터를 단 1GB의 제한된 메모리 환경에서도 중단 없이 처리하는 데 성공했으며, 기존 동기 방식 대비 초기화 속도를 약 90% 이상 단축하여 시스템의 데이터 가용성을 확보했습니다.


"Jackson Streaming API와 Redis Pipelining을 결합하여, 가용 메모리(1GB)보다 큰 대용량 JSON(911MB) 데이터를 OOM 발생 없이 효율적으로 인덱싱함."
---
### 퍼스널 컬러 분석 모델 개발

- **상황**: 이미지 기반으로 사용자의 퍼스널 컬러를 분석하는 기능이 필요
- **행동**: 이미지 분석을 통해 얼굴 추출 후 명도와 채도 추출 후 퍼스널컬러 분석, 이를 FastAPI로 배포하여 Spring 서버에서 호출 가능한 형태로 구성
- **결과**: 사용자 셀카 기반으로 컬러 분석 결과를 제공할 수 있게됨

---

### 서버 리소스 효율화를 위한 Presigned URL 적용

- **행동**: AWS S3의 Presigned URL을 이용하여 클라이언트에서 직접 업로드하도록 구현
- **결과**: 서버 트래픽을 줄이고, 이미지 처리 속도를 개선

---

### 무중단 배포 구현(예정)

- **상황**: 서비스 특성상 실시간성 유지가 중요해, 배포 중에도 서비스 중단이 발생하지 않아야 했습니다.
- **문제**: 기존에는 배포 중 도커 컨테이너를 다 내리고 이미지 삭제로 인해 서버 연결 끊김이 발생
- **행동**: 무중단 배포 전략(Rolling)을 적용하고, 서버 버전 관리 및 배포 작업의 병렬처리를 통해 배포 속도도 개선
- **결과**: 무중단 배포를 실현하고, 배포 시간은 약 40% 단축

---

### 테스트 및 API 문서화

- **행동**: 테스트 커버리지 확보를 위한 단위 테스트 작성과 함께 API 문서화도 병행
- **결과**: 팀 내 협업과 프론트엔드 연동 효율 증가
