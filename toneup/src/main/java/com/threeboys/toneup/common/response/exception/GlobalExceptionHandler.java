package com.threeboys.toneup.common.response.exception;

import com.threeboys.toneup.common.response.StandardResponse;
import com.threeboys.toneup.security.exception.InvalidRefreshTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
