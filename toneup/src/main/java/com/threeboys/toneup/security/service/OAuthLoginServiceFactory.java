package com.threeboys.toneup.security.service;

import com.threeboys.toneup.security.provider.ProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthLoginServiceFactory {
    private final Map<ProviderType, OAuthLoginService> serviceMap;

    public OAuthLoginServiceFactory(List<OAuthLoginService> services) {
        this.serviceMap = services.stream()
                .collect(Collectors.toMap(OAuthLoginService::getProviderType, Function.identity()));
    }
    public OAuthLoginService getService(ProviderType type) {
        return serviceMap.get(type); // 전략 선택
    }

}
