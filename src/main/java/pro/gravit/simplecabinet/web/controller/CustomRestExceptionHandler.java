package pro.gravit.simplecabinet.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import pro.gravit.simplecabinet.web.exception.AbstractCabinetException;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;

import java.util.Objects;

@ControllerAdvice
public class CustomRestExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(EntityNotFoundException e) {
        ApiError error = new ApiError(e.getCode(), e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AbstractCabinetException.class)
    public ResponseEntity<ApiError> handleAbstractCabinetException(AbstractCabinetException e) {
        ApiError error = new ApiError(e.getCode(), e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleSecurityException(AccessDeniedException e) {
        ApiError error = new ApiError(499, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleSecurityException(SecurityException e) {
        ApiError error = new ApiError(498, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<ApiError> handleMissingRequestValueException(MissingRequestValueException e) {
        ApiError error = new ApiError(497, e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiError> handleMultipartException(MultipartException e) {
        String message = e.getMessage();
        if (message != null){
            message = message.substring(0, message.indexOf(";")); // strip redundant information
        }
        ApiError error = new ApiError(497, message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(
                " method is not supported for this request. Supported methods are ");
        Objects.requireNonNull(ex.getSupportedHttpMethods()).forEach(t -> builder.append(t).append(" "));

        ApiError apiError = new ApiError(HttpStatus.METHOD_NOT_ALLOWED.ordinal(),
                ex.getLocalizedMessage() + builder);
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllException(Exception e) {
        e.printStackTrace();
        ApiError error = new ApiError(2000, "Internal server error. Please contact administrator");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public record ApiError(int code, String error) {
    }
}
