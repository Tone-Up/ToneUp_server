package com.threeboys.toneup.like.repository;

import com.threeboys.toneup.like.domain.ProductsLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsLikeRepository extends JpaRepository<ProductsLike, Long> {
    boolean existsByProductIdAndUserId(Long productId, Long userId);

    void deleteByProductIdAndUserId(Long productId, Long userId);

   long countByProductId(Long testProductId);
}
