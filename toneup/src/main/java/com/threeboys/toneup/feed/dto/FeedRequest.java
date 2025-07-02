package com.threeboys.toneup.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FeedRequest {
    private String content;
    private List<String> imageUrls;
}
