package com.threeboys.toneup.follow.domain;

import com.threeboys.toneup.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uniqueConstraint",
                columnNames = {"follower_id", "followee_id"} // DB 상의 column name을 작성 (변수명X)
        )
})
public class UserFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private UserEntity follower;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id")
    private UserEntity followee;

    public UserFollow(UserEntity follower, UserEntity followee) {

        this.follower = follower;
        this.followee = followee;
    }
}
