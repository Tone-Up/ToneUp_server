package com.threeboys.toneup.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProfileRequest {
    private String nickname;
    private String bio;
    private String profileImageUrl;

}
