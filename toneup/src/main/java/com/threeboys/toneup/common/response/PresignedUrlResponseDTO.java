package com.threeboys.toneup.common.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PresignedUrlResponseDTO {
    private String fileName;
    private String uploadUrl;
    private String fileUrl;
}
