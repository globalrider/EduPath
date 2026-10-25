package com.edupath.qa.forum;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(QuestionAlreadyAnsweredException.class)
    public ResponseEntity<ApiError> questionAlreadyAnswered(QuestionAlreadyAnsweredException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("QUESTION_ALREADY_ANSWERED", "Another Senior has already answered this question."));
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ApiError> questionNotFound(QuestionNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("QUESTION_NOT_FOUND", "That question could not be found."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidRequest(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ApiError("VALIDATION_ERROR", "Check the highlighted fields and try again.", fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadableRequest(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError("INVALID_REQUEST", "The request contains an invalid value or format."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError("VALIDATION_ERROR", exception.getMessage()));
    }

    public record ApiError(String code, String message, Map<String, String> fields) {
        public ApiError(String code, String message) {
            this(code, message, Map.of());
        }
    }
}
