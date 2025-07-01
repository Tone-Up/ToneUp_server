package com.threeboys.toneup.feed;

import com.threeboys.toneup.feed.domain.Feed;
import com.threeboys.toneup.feed.exception.InvalidContentLengthException;
import com.threeboys.toneup.feed.exception.InvalidImageCountException;
import com.threeboys.toneup.user.entity.UserEntity;
import com.threeboys.toneup.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FeedTest {

    @Test
    void 본문_길이_위반(){

//        List<String> imageUrls = new ArrayList<>();
//        imageUrls.add("https://cdn.example.com/uploads/img1.jpg");
//        imageUrls.add("https://cdn.example.com/uploads/img2.jpg");
        String content = "a".repeat(1001);
        assertThrows(InvalidContentLengthException.class, ()-> {
            Feed feed = Feed.builder()
                    .content(content)
                    .user(new UserEntity(1L))
                    .build();});
    }
    @Test
    void 이미지_개수_위반(){

        List<String> imageUrls = new ArrayList<>();
        for(int i =0; i<7 ;i++){
            imageUrls.add("https://cdn.example.com/uploads/img"+i+1+".jpg");
        }
        String content = "a";
        Feed feed = Feed.builder()
                .content(content)
                .user(new UserEntity(1L))
                .build();
        assertThrows(InvalidImageCountException.class, ()->feed.attchImages(imageUrls));
    }
}
