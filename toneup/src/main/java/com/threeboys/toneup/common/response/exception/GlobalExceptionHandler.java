package com.threeboys.toneup.common.response.exception;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.threeboys.toneup.common.exception.FORBIDDENException;
import com.threeboys.toneup.common.response.ErrorResponse;
import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.diary.exception.DiaryNotFoundException;
import com.threeboys.toneup.diary.exception.InvalidTitleLengthException;
import com.threeboys.toneup.feed.exception.FeedNotFoundException;
import com.threeboys.toneup.feed.exception.InvalidContentLengthException;
import com.threeboys.toneup.feed.exception.InvalidImageCountException;
import com.threeboys.toneup.security.exception.InvalidRefreshTokenException;
import com.threeboys.toneup.user.exception.DuplicateNicknameException;
import com.threeboys.toneup.user.exception.InvalidNicknameException;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<StandardResponse<Object>> handleInvalidToken(InvalidTokenException ex) {
        StandardResponse<Object> body = new StandardResponse<>(
                false, 401, ex.getMessage(), null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<StandardResponse<Object>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        StandardResponse<Object> body = new StandardResponse<>(
                false, 401, ex.getMessage(), null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler({SdkClientException.class, AmazonServiceException.class})
    public ResponseEntity<ErrorResponse<Object>> handleS3Exception(Exception ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                500, "S3_DELETE_FAILED", ErrorMessages.S3_DELETE_FAILED
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse<Object>> handleJwtException(ExpiredJwtException ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                401, "TOKEN_EXPIRED", ErrorMessages.TOKEN_EXPIRED
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(InvalidTitleLengthException.class)
    public ResponseEntity<ErrorResponse<Object>> handleDiaryException(InvalidTitleLengthException ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                400, "INVALID_TITLE_LENGTH", ErrorMessages.INVALID_TITLE_LENGTH
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    @ExceptionHandler(InvalidContentLengthException.class)
    public ResponseEntity<ErrorResponse<Object>> handleFeedAndDiaryException(InvalidContentLengthException ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                400, "INVALID_CONTENT_LENGTH", ErrorMessages.INVALID_CONTENT_LENGTH
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidImageCountException.class)
    public ResponseEntity<ErrorResponse<Object>> handleFeedAndDiaryException(InvalidImageCountException ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                400, "INVALID_IMAGE_COUNT", ErrorMessages.INVALID_IMAGE_COUNT
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    @ExceptionHandler(FORBIDDENException.class)
    public ResponseEntity<ErrorResponse<Object>> handleFeedAndDiaryException(FORBIDDENException ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                403, "FORBIDDEN", ErrorMessages.FORBIDDEN
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
    @ExceptionHandler(DiaryNotFoundException.class)
    public ResponseEntity<ErrorResponse<Object>> handleDiaryException(DiaryNotFoundException ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                404, "DIARY_NOT_FOUND", ErrorMessages.DIARY_NOT_FOUND
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(FeedNotFoundException.class)
    public ResponseEntity<ErrorResponse<Object>> handleFeedException(FeedNotFoundException ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                404, "FEED_NOT_FOUND", ErrorMessages.FEED_NOT_FOUND
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidNicknameException.class)
    public ResponseEntity<ErrorResponse<Object>> handleProfileException(InvalidNicknameException ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                400, "INVALID_NICKNAME_FORMAT", ErrorMessages.INVALID_NICKNAME_FORMAT
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ErrorResponse<Object>> handleProfileException(DuplicateNicknameException ex) {
        log.error(ex.getMessage());
        ErrorResponse<Object> body = new ErrorResponse<>(
                //추후 ErrorCode(enum 타입)으로 관리 필요
                400, "NICKNAME_ALREADY_EXISTS", ErrorMessages.NICKNAME_ALREADY_EXISTS
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }




}
