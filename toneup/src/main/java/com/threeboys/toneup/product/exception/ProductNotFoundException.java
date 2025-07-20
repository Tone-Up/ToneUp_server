package com.threeboys.toneup.product.exception;

import com.threeboys.toneup.common.response.exception.ErrorMessages;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() {
        super(ErrorMessages.PRODUCT_NOT_FOUND);
    }
}
