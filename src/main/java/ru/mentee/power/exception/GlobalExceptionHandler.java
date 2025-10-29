/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.mentee.power.dto.ErrorResponse;
import ru.mentee.power.dto.ValidationError;
import ru.mentee.power.dto.ValidationErrorResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, WebRequest request) {
        log.warn("Entity not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("NOT_FOUND");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("BAD_REQUEST");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        List<ValidationError> validationErrors =
                ex.getBindingResult().getAllErrors().stream()
                        .map(
                                error -> {
                                    ValidationError validationError = new ValidationError();
                                    if (error instanceof FieldError) {
                                        FieldError fieldError = (FieldError) error;
                                        validationError.setField(fieldError.getField());
                                        validationError.setRejectedValue(
                                                fieldError.getRejectedValue() != null
                                                        ? fieldError.getRejectedValue().toString()
                                                        : null);
                                    }
                                    validationError.setMessage(error.getDefaultMessage());
                                    return validationError;
                                })
                        .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.setError("VALIDATION_ERROR");
        errorResponse.setMessage("Ошибка валидации входных данных");
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));
        errorResponse.setValidationErrors(validationErrors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        List<ValidationError> validationErrors =
                ex.getConstraintViolations().stream()
                        .map(
                                violation -> {
                                    ValidationError validationError = new ValidationError();
                                    validationError.setField(
                                            violation.getPropertyPath().toString());
                                    validationError.setRejectedValue(
                                            violation.getInvalidValue() != null
                                                    ? violation.getInvalidValue().toString()
                                                    : null);
                                    validationError.setMessage(violation.getMessage());
                                    return validationError;
                                })
                        .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = new ValidationErrorResponse();
        errorResponse.setError("VALIDATION_ERROR");
        errorResponse.setMessage("Ошибка валидации входных данных");
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));
        errorResponse.setValidationErrors(validationErrors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("Message not readable: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("BAD_REQUEST");
        errorResponse.setMessage("Некорректный формат JSON");
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Type mismatch: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("BAD_REQUEST");
        errorResponse.setMessage("Некорректный тип параметра: " + ex.getName());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Missing parameter: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("BAD_REQUEST");
        errorResponse.setMessage("Отсутствует обязательный параметр: " + ex.getParameterName());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("Method not supported: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("METHOD_NOT_ALLOWED");
        errorResponse.setMessage("Метод " + ex.getMethod() + " не поддерживается");
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        log.warn("No handler found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("NOT_FOUND");
        errorResponse.setMessage("Эндпоинт не найден: " + ex.getRequestURL());
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError("INTERNAL_SERVER_ERROR");
        errorResponse.setMessage("Внутренняя ошибка сервера");
        errorResponse.setTimestamp(OffsetDateTime.now());
        errorResponse.setPath(getPath(request));
        errorResponse.setDetails("Обратитесь к администратору системы");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
