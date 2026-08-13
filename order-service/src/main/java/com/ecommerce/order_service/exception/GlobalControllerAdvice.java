package com.ecommerce.order_service.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleResourceNotFoundException(
      ResourceNotFoundException ex, WebRequest request) {

    log.warn(
        "Resource not found - Path {}, Message {}", request.getDescription(false), ex.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Resource Not Found");
    problemDetail.setProperty("Timestamp", Instant.now());

    problemDetail.setProperty("Resource", ex.getResourceName());
    problemDetail.setProperty("Field", ex.getFieldName());
    problemDetail.setProperty("Value", ex.getFieldValue());

    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, WebRequest request) {

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed in one or more fields");

    problemDetail.setTitle("Validation Error");
    problemDetail.setProperty("Timestamp", Instant.now());

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            error -> {
              errors.put(error.getField(), error.getDefaultMessage());
            });

    problemDetail.setProperty("Errors", errors);

    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception ex, WebRequest request) {

    log.error(
        "An unexpected error has occurred {}: {}",
        request.getDescription(false),
        ex.getMessage(),
        ex);

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error has occurred. Please contact the administrator.");

    problemDetail.setTitle("Internal Server Error");
    problemDetail.setProperty("Timestamp", Instant.now());

    return problemDetail;
  }
}
