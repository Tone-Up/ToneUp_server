package com.threeboys.toneup.search;

import com.redislabs.lettusearch.RediSearchCommands;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.lettusearch.Suggestion;
import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.repository.ImageRepository;
import com.threeboys.toneup.common.service.FileService;
import com.threeboys.toneup.product.domain.Product;
import com.threeboys.toneup.product.repository.ProductRepository;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import com.threeboys.toneup.search.dto.AutoCompleteResponse;
import com.threeboys.toneup.search.service.RedisSearchService;
import com.threeboys.toneup.search.service.SearchService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private StatefulRediSearchConnection<String, String> searchConnection;


    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageRepository imageRepository;


    @BeforeEach
    void setUp() {
        // 테스트용 상품 저장
        Product product = Product.builder()
                .productName("test")
                .brand("test")
                .sex("test")
                .type("test")
                .color("test")
                .herf("test")
                .price(0)
                .build();
        productRepository.save(product);
        Images productImage = Images.builder()
                .order(0)
                .refId(product.getId())
                .url("original-image-url")
                .s3Key("original-image-url")
                .type(ImageType.PRODUCT)
                .build();
        imageRepository.save(productImage);

        RediSearchCommands<String, String> commands = searchConnection.sync();
        String autoCompleteKey = "product-autocomplete";
        commands.sugadd(autoCompleteKey,
                        Suggestion.builder(product.getBrand()).score(5.0).build(), true);

    }

    @Test
    @DisplayName("DB검색_조회")
    void getSearchProduct_DB연동() {
        // given
        Long userId = 1L;
        String query = "test";
        Long cursor = null;
        int limit = 10;

        // when
        ProductPageItemResponse result = searchService.getSearchProduct(userId, query, cursor, limit);

        // then
        assertThat(result.getProducts()).isNotEmpty();
        assertThat(result.getProducts().getFirst().getProductName()).isEqualTo("test");
        assertThat(result.getProducts().getFirst().getBrand()).isEqualTo("test");
        assertThat(result.getProducts().getFirst().getPrice()).isEqualTo(0);
    }


    @Test
    @DisplayName("자동완성_조회")
    void getAutoComplete() {
        // given
        String keyword = "te";

        // when
        AutoCompleteResponse autoComplete = searchService.getAutoComplete(keyword);
        // then
        assertThat(autoComplete.getAutoCompleteList()).isNotEmpty();
        assertThat(autoComplete.getAutoCompleteList().getFirst().getBrandOrProductName()).isEqualTo("test");
    }
}
