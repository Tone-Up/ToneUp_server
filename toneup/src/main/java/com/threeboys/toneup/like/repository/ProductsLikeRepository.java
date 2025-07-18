package com.threeboys.toneup.like.repository;

import com.threeboys.toneup.like.domain.ProductsLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsLikeRepository extends JpaRepository<ProductsLike, Long> {
}
