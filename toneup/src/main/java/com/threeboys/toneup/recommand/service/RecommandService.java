package com.threeboys.toneup.recommand.service;

import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import com.threeboys.toneup.product.dto.ProductPreviewResponse;
import com.threeboys.toneup.product.repository.ProductRepository;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import com.threeboys.toneup.recommand.repository.ProductPersonalColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommandService {
//    private final ProductPersonalColorRepository productPersonalColorRepository;
    private final ProductRepository productRepository;
    private final ProductPersonalColorRepository productPersonalColorRepository;
    private final FileService fileService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String REDIS_KEY = "recommend:product:personalColor:";
    private final int REDIS_CACHING_SIZE = 500;
    //큰 문제점 퍼스널 컬러 재검사 후 퍼스널컬러 변경되어도 캐싱은 그대로여서 이전 퍼스널컬러에 대한 추천 상품이 조회됨(조건 넣어줘야할듯)
    public ProductPageItemResponse getProductItemPagination(Long userId, PersonalColorType personalColorType, Long cursor, int limit){
//        SELECT * FROM productpersonalcolor join product on productpersonalcolor.product_id=product.id where productpersonalcolor.personalColor_id=1 order by rand(123) limit 10 offset 29000;
//        SELECT * FROM productpersonalcolor join product on productpersonalcolor.product_id=product.id where productpersonalcolor.personalColor_id=1 and productpersonalcolor.id in (25489,14833,26214,9710,27715,3511,6702,3799,14787,27245);
        String key = REDIS_KEY+userId;
        List<Object> page = new ArrayList<>();
        boolean hasNext;
        Long nextCursor;
        Long totalSize = Optional.ofNullable(redisTemplate.opsForList().size(key)).orElse(0L);
        if(cursor==null||cursor >= totalSize){
//            int seed =
            if(cursor==null) cursor = 0L;
            System.out.println(cursor+": cursor CHECK!!!!!!!!!!!!!!!!!!!!!11");
            List<Long> randomIdList = productPersonalColorRepository.findByRandomProductIdList(personalColorType.getCode(),REDIS_CACHING_SIZE , cursor, userId);
//            List<Long> randomIdList = productPersonalColorRepository.findByPersonalColorIdAndUserId(personalColorType.getCode(), limit*50, cursor);
//            Collections.shuffle(randomIdList);
            redisTemplate.delete(key);
//            redisTemplate.opsForList().leftPushAll(key,randomIdList);
            for (int i = randomIdList.size() - 1; i >= 0; i--) {
                redisTemplate.opsForList().leftPush(key, randomIdList.get(i));
            }
            redisTemplate.expire(key, Duration.ofMinutes(10));
            page = redisTemplate.opsForList().range(key, 0, limit-1);

            //커서 초기화
//            cursor = 0L;
            nextCursor = (long) limit;
        }else{
            page = redisTemplate.opsForList().range(key, cursor, cursor+limit-1);
            nextCursor = cursor+limit;
        }


        //처음 커서가 없는 경우 특정 시드값(이 시드값을 유저별로 따로 할 지 아니면 퍼스널 컬러별로 분리 할지도 고민 필요)으로 id 리스트 전체 조회 후 캐싱 처리
          // 이 방법 아니면 서버 구동시 미리 퍼스널 컬러별로 캐싱하고 특정 시간마다 id리스트 업데이트?
        //id 리스트에서 처음 10개로 where in 해서 결과 조회 후 nextCursor 값 넣어서 보내주기(이때 값은 레디스 idx 값)
        //이후 조회는 redis에서 idx로 조회 후 id 리스트 기본 10개씩 획득 후 where in 으로 조회 반복

        List<Long> productIdList = page.stream()
                .map(o -> {
                    if (o instanceof Number) {
                        return ((Number) o).longValue();
                    } else if (o instanceof String) {
                        return Long.parseLong((String) o);
                    } else {
                        throw new IllegalArgumentException("Unexpected type in Redis list: " + o.getClass());
                    }
                })
                .collect(Collectors.toList());

        ProductPageItemResponse productPageItemResponse = productRepository.findProductWithImageAndIsLiked(userId,cursor, limit, productIdList, false);
        if(productPageItemResponse.getProducts()!=null){
            productPageItemResponse.getProducts().stream().forEach(productPreviewResponse -> {

                productPreviewResponse.setImageUrl(fileService.getPreSignedUrl(productPreviewResponse.getImageUrl()));
            });
        }

        hasNext = totalSize != null && totalSize > nextCursor;
        if(productPageItemResponse.getProducts()!=null){
            List<ProductPreviewResponse> feeds = new ArrayList<>();
            productPageItemResponse.setProducts(feeds);
            productPageItemResponse.setNextCursor(nextCursor);
            productPageItemResponse.setHasNext(hasNext);
        }else{
            productPageItemResponse.setNextCursor(null);
            productPageItemResponse.setHasNext(false);
        }

        return productPageItemResponse;

    }
}
