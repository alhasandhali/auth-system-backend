package com.backend.app.auth.authappbackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse internalError = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, 404, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(internalError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse internalError = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, 400, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(internalError);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            DisabledException.class
            })
    public ResponseEntity<ApiError> handleApiErrorException(
            Exception exception,
            HttpServletRequest request) {
        ApiError internalError = ApiError.of(exception.getMessage(), HttpStatus.BAD_REQUEST, 400, request.getRequestURI());

        return ResponseEntity.badRequest().body(internalError);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        ErrorResponse internalError = new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, 401, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(internalError);
    }
}
