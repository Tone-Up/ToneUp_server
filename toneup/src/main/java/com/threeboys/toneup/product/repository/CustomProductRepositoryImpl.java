package com.threeboys.toneup.product.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.QImages;
import com.threeboys.toneup.feed.dto.FeedPreviewResponse;
import com.threeboys.toneup.like.domain.QProductsLike;
import com.threeboys.toneup.product.domain.QProduct;
import com.threeboys.toneup.product.dto.ProductPreviewResponse;
import com.threeboys.toneup.product.dto.QProductPreviewResponse;
import com.threeboys.toneup.recommand.dto.ProductPageItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository{
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public ProductPageItemResponse findProductWithImageAndIsLiked(Long userId, Long cursor,  int limit, List<Long> randomIdList, boolean myLike) {
        //    select product.* , images.s3Key , case when productslike.id is null then false else true end as isLiked
//    from product
//    join images on product.id = images.refId and images.type = "PRODUCT"
//    left join productslike on product.id = productslike.product_id  and productslike.user_id=1
//    where product.id in(40822,28343,3098,7480,36514,1158,8825,24170,40606) and product.sex = "Female";
        QProduct product = QProduct.product;
        QImages images = QImages.images;
        QProductsLike productsLike  = QProductsLike.productsLike;

        BooleanBuilder cursorCondition = new BooleanBuilder();

        //사용자 좋아요 조회 시
        if(myLike){
            if(cursor==null) cursor = 0L;
            cursorCondition.and(productsLike.user.id.eq(userId)).and(product.id.gt(cursor));
        }else{
            if (!randomIdList.isEmpty()) {
                //랜덤 조회 시
                cursorCondition.and(product.id.in(randomIdList));
            }else{
                return new ProductPageItemResponse(null);
            }
        }

        List<ProductPreviewResponse> productPreviewResponseList = jpaQueryFactory
                .select(new QProductPreviewResponse(
                        product.id,
                        product.productName,
                        product.price,
                        images.s3Key,
                        product.herf,
                        productsLike.id.isNotNull(),
                        product.brand
                )).from(product)
                .join(images)
                .on(images.refId.eq(product.id).and(images.type.eq(ImageType.PRODUCT)))
                .leftJoin(productsLike).on(productsLike.product.eq(product),productsLike.user.id.eq(userId))
                .where(cursorCondition)
                .limit(limit+1)
                .fetch();

        boolean hasNext = productPreviewResponseList.size() > limit;
        List<ProductPreviewResponse> products = (hasNext) ? productPreviewResponseList.subList(0,productPreviewResponseList.size()-1) : productPreviewResponseList.subList(0,productPreviewResponseList.size());

        ProductPageItemResponse productPageItemResponse =new ProductPageItemResponse(products);

        if(myLike){
            Long totalCount;
            totalCount = Optional.ofNullable(
                    jpaQueryFactory
                            .select(productsLike.count())
                            .from(productsLike)
                            .where(
                                    productsLike.user.id.eq(userId)
                            )
                            .fetchOne()
            ).orElse(0L);
            Long nextCursorId = (hasNext) ? productPreviewResponseList.get(limit-1).getProductId() : null;
            productPageItemResponse.setHasNext(hasNext);
            productPageItemResponse.setNextCursor(nextCursorId);
            productPageItemResponse.setTotalCount(totalCount);
        }
        //추천 상품 페이지네이션인 경우 레디스에서 확인해서 (nextCursor, hasNext) 넣어주기
        return productPageItemResponse;
    }
}
