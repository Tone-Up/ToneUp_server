package com.threeboys.toneup.search.service;

import com.redislabs.lettusearch.Suggestion;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.product.repository.ProductRepository;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private  final ProductRepository productRepository;
    private final FileService fileService;

    private final RedisSearchService redisSearchService;

    public ProductPageItemResponse getSearchProduct(Long userId, String query, Long cursor, int limit){

        ProductPageItemResponse productPageItemResponse = productRepository.findProductWithImageAndIsLiked(userId, cursor, limit , null, false, query);
        if(productPageItemResponse.getProducts()!=null){
            productPageItemResponse.getProducts().forEach(productPreviewResponse -> {
                productPreviewResponse.setImageUrl(fileService.getPreSignedUrl(productPreviewResponse.getImageUrl()));
            });
        }
        return productPageItemResponse;
    }

    public List<Suggestion<String>> getAutoComplete(String keyword) {
        return redisSearchService.autoComplete(keyword);
    }
}
