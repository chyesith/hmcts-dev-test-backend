package uk.gov.hmcts.reform.dev.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> validationException(MethodArgumentNotValidException exception) {
        List<String> errors =
            exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        var body = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage(), errors, Instant.now());


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> taskNotFoundException(TaskNotFoundException exception) {
        var body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage(), List.of(), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> invalidRequestBody(HttpMessageNotReadableException exception) {
        String message = exception.getMessage();
        if (message != null && message.startsWith("Required request body is missing")) {
            message = "Required request body is missing"; // normalize it
        } else {
            message = "Request body is missing or contains invalid JSON";
        }

        var body = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            message,
            List.of(),
            Instant.now()
        );

        return ResponseEntity.badRequest().body(body);
    }
}
