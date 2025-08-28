package com.threeboys.toneup.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductEmbeddingRequest {
    private Long id;
    private String color;
    private String sex;
    private String type;

    @QueryProjection
    public ProductEmbeddingRequest(Long id, String color, String sex, String type) {
        this.id = id;
        this.color = color;
        this.sex = sex;
        this.type = type;
    }
}
