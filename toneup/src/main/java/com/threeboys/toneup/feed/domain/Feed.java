package com.threeboys.toneup.feed.domain;

import com.threeboys.toneup.common.domain.ImageType;
import com.threeboys.toneup.common.domain.Images;
import com.threeboys.toneup.common.exception.FORBIDDENException;
import com.threeboys.toneup.feed.exception.InvalidContentLengthException;
import com.threeboys.toneup.feed.exception.InvalidImageCountException;
import com.threeboys.toneup.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
//@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
public class Feed {
    private static final int MAX_CONTENT_LENGTH = 1000;
    private static final int MAX_IMAGE_SIZE = 5;
//    private static final int MIN_CONTENT_LENGTH = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String content;

    private int likeCount;

    @Transient
    private List<Images> imageUrlList = new ArrayList<>();

    @Builder
    public Feed(UserEntity user , String content){
        validateContent(content);
        this.user = user;
        this.content = content;
        this.likeCount = 0;
    }

    public void attachImages(List<String> imageUrls){
        validateImageCount(imageUrls);
        for(int i =0; i< imageUrls.size(); i++){
            Images images = Images.builder()
                    .type(ImageType.FEED)
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

    public void changeFeed(String content, List<String> imageUrls) {
        validateContent(content);
        validateImageCount(imageUrls);
        this.content = content;
        attachImages(imageUrls);
    }

    public void validateOwner(Long userId) {
        if (!this.user.getId().equals(userId)) {
            throw new FORBIDDENException();
        }
    }

    public void decreaseLikeCount() {
        this.likeCount -=1;
    }

    public void increaseLikeCount() {
        this.likeCount +=1;
    }
}
