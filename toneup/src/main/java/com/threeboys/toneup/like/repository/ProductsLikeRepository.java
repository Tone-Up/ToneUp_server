package com.threeboys.toneup.like.repository;

import com.threeboys.toneup.like.domain.ProductsLike;
import com.threeboys.toneup.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsLikeRepository extends JpaRepository<ProductsLike, Long> {
    boolean existsByProductIdAndUserId(Long productId, Long userId);

    void deleteByProductIdAndUserId(Long productId, Long userId);

   long countByProductId(Long testProductId);

    List<ProductsLike> findTop5ByUserIdOrderByIdDesc(Long userId);
}
