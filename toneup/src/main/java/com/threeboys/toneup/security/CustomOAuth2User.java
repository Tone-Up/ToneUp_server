package com.threeboys.toneup.security;

import com.threeboys.toneup.user.dto.UserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final UserDTO userDTO;

    public CustomOAuth2User(UserDTO userDTO) {

        this.userDTO = userDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return userDTO.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getName() {

        return userDTO.getName();
    }

    public String getNickname() {

        return userDTO.getNickname();
    }

    public String getPersonalColor() {

        return userDTO.getPersonalColor();
    }
    public String getRole() {

        return userDTO.getRole();
    }
    public Long getId() {

        return userDTO.getId();
    }
    public String getProvider() {

        return userDTO.getProvider();
    }
}