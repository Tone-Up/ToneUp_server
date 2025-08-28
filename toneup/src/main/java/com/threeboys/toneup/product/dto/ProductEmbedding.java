package com.threeboys.toneup.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductEmbedding {
    @JsonProperty("product_id")
    private Long productId;

    private float[] embedding;
}
