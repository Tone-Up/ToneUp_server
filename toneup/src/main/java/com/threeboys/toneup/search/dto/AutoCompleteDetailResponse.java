package com.threeboys.toneup.search.dto;

import lombok.Data;

@Data
public class AutoCompleteDetailResponse {
    private String brandOrProductName;

    public AutoCompleteDetailResponse(String brandOrProductName) {
        this.brandOrProductName = brandOrProductName;
    }
}
