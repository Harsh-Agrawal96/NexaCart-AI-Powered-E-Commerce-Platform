package com.n11bootcamp.common.exception;

import com.n11bootcamp.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleCoreException(CoreException ex) {
        BaseErrorCode error = ex.getErrorCode();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                error.getHttpStatus(),
                resolveMessage(error.getMessageKey(), ex.getArgs())
        );
        problem.setTitle(resolveMessage(error.getTitleKey()));
        problem.setProperty("code", error.getCode());

        return ResponseEntity.status(error.getHttpStatus()).body(ApiResponse.error(problem));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle(resolveMessage("validation.error.title"));
        problem.setDetail(resolveMessage("validation.error.detail"));
        problem.setProperty("code", "VALIDATION-400");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(ApiResponse.error(problem));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle(resolveMessage("validation.error.title"));
        problem.setDetail(String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName()));
        problem.setProperty("code", "VALIDATION-400");

        return ResponseEntity.badRequest().body(ApiResponse.error(problem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle(resolveMessage("validation.error.title"));
        problem.setDetail(resolveMessage("validation.malformed.json"));
        problem.setProperty("code", "VALIDATION-400");

        return ResponseEntity.badRequest().body(ApiResponse.error(problem));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ProblemDetail>> handleGeneral(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle(resolveMessage("error.internal.title"));
        problem.setDetail(resolveMessage("error.internal.detail"));
        problem.setProperty("code", "SERVER-500");

        return ResponseEntity.internalServerError().body(ApiResponse.error(problem));
    }

    private String resolveMessage(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return key;
        }
    }
}