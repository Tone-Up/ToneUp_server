package com.threeboys.toneup.diary.domain;


import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.exception.FORBIDDENException;
import com.threeboys.toneup.diary.exception.InvalidTitleLengthException;
import com.threeboys.toneup.feed.exception.InvalidContentLengthException;
import com.threeboys.toneup.feed.exception.InvalidImageCountException;
import com.threeboys.toneup.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@NoArgsConstructor
@Entity
@Getter
public class Diary {
    private static final int MAX_CONTENT_LENGTH = 1000;
    private static final int MAX_IMAGE_SIZE = 5;
    private static final int MAX_TITLE_LENGTH = 100;
//    private static final int MIN_CONTENT_LENGTH = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userId;

    private String content;

    private String title;

    @Transient
    private List<Images> imageUrlList = new ArrayList<>();

    @Builder
    public Diary(UserEntity user , String title, String content){
        validatetitle(title);
        validateContent(content);
        this.userId = user;
        this.title = title;
        this.content = content;
    }

    public void attachImages(List<String> imageUrls){
        validateImageCount(imageUrls);
        for(int i =0; i< imageUrls.size(); i++){
            Images images = Images.builder()
                    .type(ImageType.DIARY)
                    .refId(id)
                    .url(imageUrls.get(i))
                    .order(i)
                    .s3Key(imageUrls.get(i))
                    .build();
            imageUrlList.add(images);
        }

    }

    private void validateImageCount(List<String> imageUrls){
        if(imageUrls==null|| imageUrls.isEmpty() || imageUrls.size()>MAX_IMAGE_SIZE){
            throw new InvalidImageCountException();
        }
    }
    private void validateContent(String content) {
        if(content==null|| content.isEmpty() || content.length()>MAX_CONTENT_LENGTH){
            throw new InvalidContentLengthException();
        }
    }
    private void validatetitle(String content) {
        if(content==null|| content.isEmpty() || content.length()>MAX_TITLE_LENGTH){
            throw new InvalidTitleLengthException();
        }
    }

    public void changeDiary(String title, String content, List<String> imageUrls) {
        validatetitle(title);
        validateContent(content);
        validateImageCount(imageUrls);
        this.title = title;
        this.content = content;
        attachImages(imageUrls);
    }

    public void validateOwner(Long userId) {
        if (!this.userId.getId().equals(userId)) {
            throw new FORBIDDENException();
        }
    }
}

