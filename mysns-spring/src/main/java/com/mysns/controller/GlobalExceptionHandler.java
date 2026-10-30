package com.mysns.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.mysns.dto.ErrorResponse;
import com.mysns.exception.DuplicateUserException;
import com.mysns.exception.InvalidCredentialsException;
import com.mysns.exception.UnauthorizedException;
import com.mysns.exception.UserNotFoundException;
import com.mysns.exception.ValidationException;

/** 도메인 예외를 HTTP 상태 코드로 번역한다. 스택 트레이스는 응답에 싣지 않는다. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> validation(ValidationException e) {
        return body(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> invalidBody(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");
        return body(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadable(HttpMessageNotReadableException e) {
        return body(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다.");
    }

    @ExceptionHandler({UnauthorizedException.class, InvalidCredentialsException.class})
    public ResponseEntity<ErrorResponse> unauthorized(RuntimeException e) {
        return body(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(UserNotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponse> conflict(DuplicateUserException e) {
        return body(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tooLarge(MaxUploadSizeExceededException e) {
        return body(HttpStatus.CONTENT_TOO_LARGE, "업로드 용량 제한을 초과했습니다.");
    }

    private ResponseEntity<ErrorResponse> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }
}
