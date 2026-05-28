package org.example.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.model.BadCredentialsException;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.exception.model.SecurityException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException exc) {
        ErrorResponse errorResponse = ErrorResponse.of(exc.getType(), exc.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurityException(SecurityException exc) {
        ErrorResponse errorResponse = ErrorResponse.of(exc.getType(), exc.getMessage(), List.of());

        HttpStatus statusCode = HttpStatus.FORBIDDEN;

        if(exc instanceof BadCredentialsException) {
            statusCode = HttpStatus.UNAUTHORIZED;
        }
        return ResponseEntity.status(statusCode)
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exc) {
        var errors = exc.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> new ErrorResponse.Error(err.getDefaultMessage(), err.getField()))
                .toList();

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorResponse.ErrorType.VALIDATION_ERROR,
                "Validation failed",
                errors
        );

        log.warn("REST_ERROR status={} type={} message={} errorsCount={}",
                HttpStatus.BAD_REQUEST.value(),
                ErrorResponse.ErrorType.VALIDATION_ERROR,
                "Validation failed",
                errors.size());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

}


