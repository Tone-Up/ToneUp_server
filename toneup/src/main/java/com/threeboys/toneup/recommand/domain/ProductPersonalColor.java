package com.threeboys.toneup.recommand.domain;

import com.threeboys.toneup.personalColor.domain.PersonalColor;
import com.threeboys.toneup.product.domain.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uniqueConstraint",
                columnNames = {"personalColor_id", "product_id"} // DB 상의 column name을 작성 (변수명X)
        )
})
public class ProductPersonalColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personalColor_id")
    private PersonalColor personalColor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

}
