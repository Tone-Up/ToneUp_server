package com.threeboys.toneup.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.QImages;
import com.threeboys.toneup.like.domain.QProductsLike;
import com.threeboys.toneup.product.domain.QProduct;
import com.threeboys.toneup.product.dto.ProductPreviewResponse;
import com.threeboys.toneup.product.dto.QProductPreviewResponse;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@RequiredArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository{
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public ProductPageItemResponse findProductWithImageAndIsLiked(Long userId, Long cursor,  int limit, List<Long> randomIdList) {
        //    select product.* , images.s3Key , case when productslike.id is null then false else true end as isLiked
//    from product
//    join images on product.id = images.refId and images.type = "PRODUCT"
//    left join productslike on product.id = productslike.product_id  and productslike.user_id=1
//    where product.id in(40822,28343,3098,7480,36514,1158,8825,24170,40606) and product.sex = "Female";
        QProduct product = QProduct.product;
        QImages images = QImages.images;
        QProductsLike productsLike  = QProductsLike.productsLike;

        BooleanBuilder cursorCondition = new BooleanBuilder();
        if (!randomIdList.isEmpty()) {
            cursorCondition.or(product.id.in(randomIdList));
        }else{
            return new ProductPageItemResponse(null);
        }

        List<ProductPreviewResponse> productPreviewResponseList = jpaQueryFactory
                .select(new QProductPreviewResponse(
                        product.id,
                        product.productName,
                        product.price,
                        images.s3Key,
                        product.herf,
                        productsLike.id.isNotNull()
                )).from(product)
                .join(images)
                .on(images.refId.eq(product.id).and(images.type.eq(ImageType.PRODUCT)))
                .leftJoin(productsLike).on(productsLike.product.eq(product),productsLike.user.id.eq(userId))
                .where(cursorCondition)
                .fetch();
                //레디스에서 확인해서 (nextCursor, hasNext) 넣어주기
        return new ProductPageItemResponse(productPreviewResponseList);
    }
}
