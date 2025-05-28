//package com.threeboys.toneup.security;
//
//import com.threeboys.toneup.security.provider.ProviderType;
//import com.threeboys.toneup.security.service.CustomOAuth2UserService;
//import com.threeboys.toneup.user.domain.User;
//import com.threeboys.toneup.user.entity.UserEntity;
//import com.threeboys.toneup.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.oauth2.client.registration.ClientRegistration;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//
//import java.util.Map;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//
//// 단위 테스트
//@ExtendWith(MockitoExtension.class)
//class CustomOAuth2UserServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private DefaultOAuth2UserService defaultOAuth2UserService;
//    private CustomOAuth2UserService oAuth2UserService;
//
//    @BeforeEach
//    void setup() {
//        oAuth2UserService = new CustomOAuth2UserService(userRepository);
//    }
//
//    @Test
//    void testProcessOAuth2User_existingUser() {
//        OAuth2UserRequest mockRequest = mock(OAuth2UserRequest.class);
//        OAuth2User mockOAuth2User = mock(OAuth2User.class);
//
//        when(defaultOAuth2UserService.loadUser(mockRequest)).thenReturn(mockOAuth2User);
//        when(mockOAuth2User.getAttribute("providerType")).thenReturn("google");
//        when(mockOAuth2User.getAttribute("providerId")).thenReturn("1234");
//        UserEntity newUser = new UserEntity("김준용","김준용_1234", ProviderType.GOOGLE, "1234", "test@test.com");
//        ProviderType providerType = ProviderType.GOOGLE;
//        when(userRepository.findByProviderAndProviderId(providerType,"1234")).thenReturn(Optional.of(newUser));
//
//        OAuth2User result = oAuth2UserService.loadUser(mockRequest);
//
//        assertNotNull(result);
//        verify(userRepository, never()).save(any());
//    }

//    @Test
//    void testProcessOAuth2User_newUser() {
//        OAuth2User mockOAuth2User = mock(OAuth2User.class);
//        when(mockOAuth2User.getAttribute("email")).thenReturn("new@example.com");
//
//        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
//
//        User newUser = new User("new@example.com");
//        when(userRepository.save(any(User.class))).thenReturn(newUser);
//
//        User result = oAuth2UserService.processOAuth2User(mockOAuth2User);
//
//        assertEquals(newUser, result);
//        verify(userRepository).save(any(User.class));
//    }
//}