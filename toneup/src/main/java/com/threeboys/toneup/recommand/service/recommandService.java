package com.threeboys.toneup.recommand.service;

import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import com.threeboys.toneup.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class recommandService {
//    private final ProductPersonalColorRepository productPersonalColorRepository;
    private final ProductRepository productRepository;


    public void productItemPagination(PersonalColorType personalColorType, Long cursor){
//        SELECT * FROM productpersonalcolor join product on productpersonalcolor.product_id=product.id where productpersonalcolor.personalColor_id=1 order by rand(123) limit 10 offset 29000;
//        SELECT * FROM productpersonalcolor join product on productpersonalcolor.product_id=product.id where productpersonalcolor.personalColor_id=1 and productpersonalcolor.id in (25489,14833,26214,9710,27715,3511,6702,3799,14787,27245);

        //처음 커서가 없는 경우 특정 시드값(이 시드값을 유저별로 따로 할 지 아니면 퍼스널 컬러별로 분리 할지도 고민 필요)으로 id 리스트 전체 조회 후 캐싱 처리
          // 이 방법 아니면 서버 구동시 미리 퍼스널 컬러별로 캐싱하고 특정 시간마다 id리스트 업데이트?
        //id 리스트에서 처음 10개로 where in 해서 결과 조회 후 nextCursor 값 넣어서 보내주기(이때 값은 레디스 idx 값)
        //이후 조회는 redis에서 idx로 조회 후 id 리스트 기본 10개씩 획득 후 where in 으로 조회 반복
    }
}
