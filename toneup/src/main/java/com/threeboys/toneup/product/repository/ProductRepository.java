package com.threeboys.toneup.product.repository;

import com.threeboys.toneup.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, CustomProductRepository {

    @Query("SELECT p.productName " +
            "FROM Product p " +
            "WHERE p.productName LIKE %:kw% OR p.brand LIKE %:kw% " +
            "GROUP BY p.productName " +
            "ORDER BY SUM(CASE WHEN p.brand LIKE %:kw% THEN 5 ELSE 1 END) DESC")
    List<String> searchByKeyword(@Param("kw") String keyword);
}
