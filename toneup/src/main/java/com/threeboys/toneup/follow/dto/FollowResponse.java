package com.threeboys.toneup.follow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowResponse {
    @JsonProperty("isFollower")
    private boolean isFollower;
    @JsonProperty("isFollowing")
    private boolean isFollowing;
}
