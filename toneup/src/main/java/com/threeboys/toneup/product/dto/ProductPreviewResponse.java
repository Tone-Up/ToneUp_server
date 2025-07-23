package com.threeboys.toneup.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ProductPreviewResponse {
    private Long productId;
    private String productName;
    private int price;
    private String imageUrl;
    private String productDetailUrl;
    private boolean isLiked;

    @QueryProjection
    public ProductPreviewResponse(Long productId, String productName, int price, String imageUrl, String productDetailUrl, boolean isLiked) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.productDetailUrl = productDetailUrl;
        this.isLiked = isLiked;
    }
}
