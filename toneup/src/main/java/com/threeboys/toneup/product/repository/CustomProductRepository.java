package com.threeboys.toneup.product.repository;

import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;

import java.util.List;

public interface CustomProductRepository {
    ProductPageItemResponse findProductWithImageAndIsLiked(Long userId, Long cursor,  int limit, List<Long> randomIdList, boolean myLike);
}
