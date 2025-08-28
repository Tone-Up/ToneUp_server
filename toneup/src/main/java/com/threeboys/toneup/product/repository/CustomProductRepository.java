package com.threeboys.toneup.product.repository;

import com.threeboys.toneup.product.dto.ProductEmbeddingRequest;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomProductRepository {
    ProductPageItemResponse findProductWithImageAndIsLiked(Long userId, Long cursor,  int limit, List<Long> randomIdList, boolean myLike, String query);
    List<ProductEmbeddingRequest> findAllEmbeddingData();
}
