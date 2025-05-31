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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@Service
@RequiredArgsConstructor
public class PersonalColorService {
    private final FastApiClient fastApiClient;
    private final UserRepository userRepository;
    private final PersonalColorRepository personalColorRepository;


    @Transactional
    public String updatePersonalColor(Long userId, MultipartFile image) {
        // 1. 유저 조회
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. FastAPI 요청 객체 생성
        PersonalColorAnalyzeRequest request = new PersonalColorAnalyzeRequest(userEntity.getId(), image);

        // 3. FastAPI 결과 받아서 도메인 객체 생성
        PersonalColorAnalyzeResponse personalColor = fastApiClient.requestPersonalColorUpdate(request);

        // 4. 퍼스널컬러 영속 엔티티 조회 (연관관계용)
        PersonalColor colorEntity = personalColorRepository.findByPersonalColorType(personalColor.getPersonalColor())
                .orElseThrow(() -> new PersonalColorNotFoundException(personalColor.getPersonalColor()));

        // 5. 유저에 퍼스널컬러 반영 (User 도메인에서 처리)
        userEntity.updatePersonalColor(colorEntity);
        return personalColor.getPersonalColor().name();
    }



}
