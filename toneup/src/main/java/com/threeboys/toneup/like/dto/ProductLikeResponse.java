package com.threeboys.toneup.like.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductLikeResponse {
    private Long productId;
    private boolean isLiked;
}
