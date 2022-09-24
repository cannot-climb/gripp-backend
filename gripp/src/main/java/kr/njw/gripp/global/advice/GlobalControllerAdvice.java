package kr.njw.gripp.global.advice;

import kr.njw.gripp.global.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrors(e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> (error.getField() + " " +
                        Objects.requireNonNullElse(error.getDefaultMessage(), "")).trim())
                .collect(Collectors.toList()));

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
