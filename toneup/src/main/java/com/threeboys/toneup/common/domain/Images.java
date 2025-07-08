package com.threeboys.toneup.common.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Images {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long refId;
    private String url;

    @Enumerated(EnumType.STRING)
    private ImageType type;
    private int ImageOrder;
    private String s3Key;

    @Builder
    public Images(Long refId, String url, ImageType type, int order, String s3Key) {
        this.refId = refId;
        this.url = url;
        this.type = type;
        this.ImageOrder = order;
        this.s3Key = s3Key;
    }

    public void changeProfileImageUrl(String url){
        this.url = url;
    }
}
