package com.threeboys.toneup.recommand.repository;

import com.threeboys.toneup.personalColor.domain.PersonalColorType;
import com.threeboys.toneup.recommand.domain.ProductPersonalColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPersonalColorRepository extends JpaRepository<ProductPersonalColor, Long> {

    @Query(value = "select product.id from ProductPersonalColor " +
            "join product on  ProductPersonalColor.product_id=product.id " +
            "where ProductPersonalColor.personalColor_id=:personalColorId " +
            "order by rand(:seed) limit :limit offset :offset", nativeQuery = true)
    List<Long> findByRandomProductIdList(@Param("personalColorId")int personalColorId, @Param("limit")int limit, @Param("offset")Long offset, @Param("seed") Long seed);


    @Query(value = "select product.id from ProductPersonalColor join product on  ProductPersonalColor.product_id=product.id where ProductPersonalColor.personalColor_id=:personalColorId limit :limit offset :offset", nativeQuery = true)
    List<Long> findByPersonalColorIdAndUserId(@Param("personalColorId")int personalColorId, @Param("limit")int limit, @Param("offset")Long offset);

}
