package com.threeboys.toneup.common.response.exception;

public class ErrorMessages {
    public static final String INVALID_SOCIAL_TOKEN = "Invalid social token";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String PERSONAL_COLOR_NOT_FOUND = "PersonalColor not found with type: %s";
    public static final String ROOM_NOT_FOUND = "Room not found";
    public static final String INVALID_REFRESH_TOKEN = "유효하지 않은 리프레쉬 토큰입니다.";
    public static final String INVALID_CONTENT_LENGTH = "본문은 최소 1글자 이상 최대 1000자까지 작성 가능합니다.";
    public static final String INVALID_IMAGE_COUNT = "이미지 URL은 최소 1개 이상 최대 5개까지 가능합니다.";
    public static final String FEED_NOT_FOUND = "피드를 찾을 수 없습니다.";
    public static final String FORBIDDEN = "작성자 본인이 아니여서 수정할 권한이 없습니다.";
    public static final String S3_DELETE_FAILED = "s3 이미지 삭제 중 오류가 발생했습니다.";
    public static final String EXPIRED_SOCIAL_TOKEN = "만료된 소셜 로그인 토큰입니다.";
    public static final String INVALID_TITLE_LENGTH = "제목은 1자 이상 100자 이하여야 합니다.";
    public static final String DIARY_NOT_FOUND = "존재하지 않는 다이어리입니다.";
    public static final String TOKEN_EXPIRED = "토큰이 만료되었습니다.";
}
