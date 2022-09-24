package kr.njw.gripp.global.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, Object> body = new HashMap<>();

        body.put("messages", e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> (error.getField() + " " +
                        Objects.requireNonNullElse(error.getDefaultMessage(), "")).trim()));

        return ResponseEntity.badRequest().body(body);
    }
}
