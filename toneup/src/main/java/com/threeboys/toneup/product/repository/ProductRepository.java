package com.threeboys.toneup.product.repository;

import com.threeboys.toneup.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, CustomProductRepository {

}
