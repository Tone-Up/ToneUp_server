package com.threeboys.toneup.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class PresignedUrlListResponseDTO {
    private List<PresignedUrlResponseDTO> files;
}
