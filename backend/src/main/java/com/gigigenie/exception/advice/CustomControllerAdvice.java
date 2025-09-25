package com.gigigenie.exception.advice;

import com.gigigenie.exception.CustomJWTException;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class CustomControllerAdvice {

    // 404: Optional.get() 등에서 값이 없을 때
    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<?> handleNoSuchElementException(NoSuchElementException e) {
        String msg = e.getMessage();
        log.error("NoSuchElementException: {}", msg);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getErrorMessage(msg));
    }

    // 404: JPA 조회 시 엔티티 미존재
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e) {
        String msg = e.getMessage();
        log.error("EntityNotFoundException: {}", msg);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getErrorMessage(msg));
    }

    // 400: 잘못된 파라미터
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        String msg = e.getMessage();
        log.error("IllegalArgumentException: {}", msg);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getErrorMessage(msg));
    }

    // 400: @Valid 바인딩 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        log.error("MethodArgumentNotValidException: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }


    // 400: 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?>
    handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException
        ex) {
        String errorMessage = String.format(
            "Invalid argument: '%s'. Expected type: '%s'.",
            ex.getValue(),
            ex.getRequiredType().getSimpleName()
        );
        log.error("MethodArgumentTypeMismatchException: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getErrorMessage(errorMessage));
    }

    // 400: 요청 바디가 JSON 형식이 아닐 때 발생
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?>
    handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String msg = e.getMessage();
        log.error("HttpMessageNotReadableException: {}", msg);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getErrorMessage(msg));
    }

    // 401: JWT 인증 실패
    @ExceptionHandler(CustomJWTException.class)
    protected ResponseEntity<?> handleJWTException(CustomJWTException e) {
        String msg = e.getMessage();
        log.error("CustomJWTException: {}", msg);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(getErrorMessage(msg));
    }

    // 401: 비밀번호 불일치
    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        String msg = e.getMessage();
        log.error("BadCredentialsException: {}", msg);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(getErrorMessage(msg));
    }

    // 그외 나머지 exception들은 모두 이곳에서 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Exception e) {
        String msg = e.getMessage();
        log.error("Exception: {}", msg);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getErrorMessage(msg));
    }

    private static Map<String, String> getErrorMessage(String msg) {
        return Map.of("errMsg", msg);
    }

}
