package com.threeboys.toneup.recommand.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.threeboys.toneup.feed.dto.FeedPreviewResponse;
import com.threeboys.toneup.product.dto.ProductPreviewResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductPageItemResponse {
    private List<ProductPreviewResponse> products;
    private Long nextCursor;
    private boolean hasNext;
    private Long totalCount;

    public ProductPageItemResponse(List<ProductPreviewResponse> products) {
        this.products = products;
    }

    //    private Long totalCount;
}
