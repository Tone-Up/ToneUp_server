package com.threeboys.toneup.search.service;

import com.redislabs.lettusearch.Suggestion;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.product.domain.Product;
import com.threeboys.toneup.product.repository.ProductRepository;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import com.threeboys.toneup.search.dto.AutoCompleteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private  final ProductRepository productRepository;
    private final FileService fileService;

    private final RedisSearchService redisSearchService;
    @Transactional(readOnly = true)
    public ProductPageItemResponse getSearchProduct(Long userId, String query, Long cursor, int limit){

        ProductPageItemResponse productPageItemResponse = productRepository.findProductWithImageAndIsLiked(userId, cursor, limit , null, false, query);
        if(productPageItemResponse.getProducts()!=null){
            productPageItemResponse.getProducts().forEach(productPreviewResponse -> {
                productPreviewResponse.setImageUrl(fileService.getPreSignedUrl(productPreviewResponse.getImageUrl()));
            });
        }
        return productPageItemResponse;
    }
    @Transactional(readOnly = true)
    public AutoCompleteResponse getAutoComplete(String keyword) {
        List<Suggestion<String>> suggestionList =  redisSearchService.autoComplete(keyword);
        return  AutoCompleteResponse.toDto(suggestionList);
    }

    public List<String> getSearch(String keyword) {
        return  productRepository.searchByKeyword(keyword);
    }
}
