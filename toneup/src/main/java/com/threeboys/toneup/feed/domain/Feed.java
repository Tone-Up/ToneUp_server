package com.threeboys.toneup.feed.domain;

import com.threeboys.toneup.feed.exception.InvalidContentLengthException;
import com.threeboys.toneup.feed.exception.InvalidImageCountException;
import com.threeboys.toneup.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;
//@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Feed {
    private static final int MAX_CONTENT_LENGTH = 1000;
    private static final int MAX_IMAGE_SIZE = 5;
//    private static final int MIN_CONTENT_LENGTH = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userId;

    private String content;
    @Transient
    private List<String> imageUrls;

    @Builder
    public Feed(UserEntity user , String content){
        validateContent(content);
        this.userId = user;
        this.content = content;
    }

    public void attchImages(List<String> imageUrls){
        validateImageCount(imageUrls);
        this.imageUrls = imageUrls;
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
}
