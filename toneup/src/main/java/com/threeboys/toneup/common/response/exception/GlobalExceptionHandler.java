package com.threeboys.toneup.common.response.exception;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.threeboys.toneup.common.response.ErrorResponse;
import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.exception.InvalidRefreshTokenException;
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
                401, "EXPIRED_SOCIAL_TOKEN", ErrorMessages.EXPIRED_SOCIAL_TOKEN
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

}
