/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.controller;

import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mentee.power.api.generated.dto.ErrorResponse;
import ru.mentee.power.api.generated.dto.ErrorResponseError;
import ru.mentee.power.api.generated.dto.ErrorResponseErrorDetailsInner;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponseErrorDetailsInner> details =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(this::toDetail)
                        .collect(Collectors.toList());
        return buildError(
                HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Неверные параметры запроса", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex) {
        List<ErrorResponseErrorDetailsInner> details =
                ex.getConstraintViolations().stream()
                        .map(
                                v -> {
                                    ErrorResponseErrorDetailsInner d =
                                            new ErrorResponseErrorDetailsInner();
                                    d.setField(v.getPropertyPath().toString());
                                    d.setMessage(v.getMessage());
                                    return d;
                                })
                        .toList();
        return buildError(
                HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Неверные параметры запроса", details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IllegalArgumentException ex) {
        return buildError(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND", "Книга не найдена", null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException ex) {
        return buildError(
                HttpStatus.CONFLICT,
                "BOOK_ALREADY_EXISTS",
                "Книга с таким ISBN уже существует",
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Внутренняя ошибка сервера",
                null);
    }

    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String code,
            String message,
            List<ErrorResponseErrorDetailsInner> details) {
        ErrorResponseError error = new ErrorResponseError();
        error.setCode(code);
        error.setMessage(message);
        if (details != null) {
            error.setDetails(details);
        }
        ErrorResponse response = new ErrorResponse();
        response.setSuccess(false);
        response.setError(error);
        response.setTimestamp(OffsetDateTime.now());
        return ResponseEntity.status(status).body(response);
    }

    private ErrorResponseErrorDetailsInner toDetail(FieldError fe) {
        ErrorResponseErrorDetailsInner d = new ErrorResponseErrorDetailsInner();
        d.setField(fe.getField());
        d.setMessage(fe.getDefaultMessage());
        return d;
    }
}
