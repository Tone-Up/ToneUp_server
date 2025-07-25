package com.threeboys.toneup.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductPreviewResponse {
    private Long productId;
    private String brand;
    private String productName;
    private int price;
    private String imageUrl;
    private String productDetailUrl;
    @JsonProperty("isLiked")
    private boolean isLiked;

    @QueryProjection
    public ProductPreviewResponse(Long productId, String productName, int price, String imageUrl, String productDetailUrl, boolean isLiked, String brand) {
        this.productId = productId;
        this.brand = brand;
        this.productName = productName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.productDetailUrl = productDetailUrl;
        this.isLiked = isLiked;
    }
}
