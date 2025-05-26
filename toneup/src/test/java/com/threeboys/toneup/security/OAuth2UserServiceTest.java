//package com.threeboys.toneup.security;
//
//import com.threeboys.toneup.security.service.CustomOAuth2UserService;
//import com.threeboys.toneup.user.domain.User;
//import com.threeboys.toneup.user.repository.UserRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.oauth2.client.registration.ClientRegistration;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//
//import java.util.Map;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.BDDMockito.given;
//
//@ExtendWith(MockitoExtension.class)
//class OAuth2UserServiceTest {
//
//    @InjectMocks
//    private CustomOAuth2UserService oAuth2UserService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private OAuth2UserRequest userRequest;
//
//    @Mock
//    private ClientRegistration clientRegistration;
//    @Mock
//    private OAuth2User oauth2User;
//
//    @Test
//    void returns_existing_user() {
//        // given
//        User existUser = new User("test");
//
//        given(oauth2User.getAttributes()).willReturn(Map.of(
//                "email", "test@example.com",
//                "name", "테스트유저"
//        ));
//
//        given(userRequest.getClientRegistration()).willReturn(clientRegistration);
//        given(clientRegistration.getRegistrationId()).willReturn("google");
//        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(existUser));
//
//        // when
//        OAuth2User user = oAuth2UserService.loadUser(userRequest);
//
//        // then
//        assertThat(user.getName()).isEqualTo("test@example.com");
//    }
//}
