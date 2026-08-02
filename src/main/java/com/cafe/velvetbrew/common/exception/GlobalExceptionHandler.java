package com.cafe.velvetbrew.common.exception;

import com.cafe.velvetbrew.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
	        MethodArgumentNotValidException ex) {

	    Map<String, String> errors = new HashMap<>();

	    ex.getBindingResult().getFieldErrors().forEach(error ->
	            errors.put(error.getField(), error.getDefaultMessage()));

	    return ResponseEntity.badRequest()
	            .body(ApiResponse.<Map<String, String>>builder()
	                    .success(false)
	                    .message("Validation failed")
	                    .data(errors)
	                    .timestamp(LocalDateTime.now())
	                    .build());
	}
	


	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<Object>> handleBadCredentials(
	        BadCredentialsException ex) {

	    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	            .body(ApiResponse.builder()
	                    .success(false)
	                    .message("Invalid email or password")
	                    .timestamp(LocalDateTime.now())
	                    .build());
	}
	
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailExists(
            EmailAlreadyExistsException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(
            Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());

    }
    
    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleCategoryExists(
            CategoryAlreadyExistsException ex) {

        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MenuItemAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleMenuExists(
            MenuItemAlreadyExistsException ex) {

        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
    
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomerNotFound(CustomerNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFound(
            OrderNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
}