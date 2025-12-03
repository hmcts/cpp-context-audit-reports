package uk.gov.hmcts.cpp.audit.bff.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleHttpClientErrorException(HttpClientErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> handleHttpServerErrorException(HttpServerErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }
}
