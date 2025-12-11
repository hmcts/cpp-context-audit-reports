package uk.gov.hmcts.cpp.audit.bff.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import uk.gov.hmcts.cpp.audit.bff.model.ErrorResponse;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest mockRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        mockRequest = mock(WebRequest.class);
    }

    @Test
    void testHandleHttpClientErrorExceptionBadRequest() {
        String errorMessage = "Invalid request parameters";
        HttpClientErrorException exception = new HttpClientErrorException(
            HttpStatus.BAD_REQUEST,
            errorMessage
        );
        when(mockRequest.getDescription(false)).thenReturn("uri=/test-endpoint");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn("correlation-123");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleHttpClientErrorException(exception, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("/test-endpoint", response.getBody().path());
        assertEquals("correlation-123", response.getBody().correlationId());
    }

    @Test
    void testHandleHttpClientErrorExceptionUnauthorized() {
        String errorMessage = "Unauthorized access";
        HttpClientErrorException exception = new HttpClientErrorException(
            HttpStatus.UNAUTHORIZED,
            errorMessage
        );
        when(mockRequest.getDescription(false)).thenReturn("uri=/secure-endpoint");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn(null);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleHttpClientErrorException(exception, mockRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().status());
        assertEquals("Unauthorized", response.getBody().error());
        assertEquals("N/A", response.getBody().correlationId());
    }

    @Test
    void testHandleHttpClientErrorExceptionForbidden() {
        HttpClientErrorException exception = new HttpClientErrorException(
            HttpStatus.FORBIDDEN,
            "Access denied"
        );
        when(mockRequest.getDescription(false)).thenReturn("uri=/admin");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn("corr-456");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleHttpClientErrorException(exception, mockRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().status());
        assertEquals("Forbidden", response.getBody().error());
    }

    @Test
    void testHandleHttpClientErrorExceptionNotFound() {
        HttpClientErrorException exception = new HttpClientErrorException(
            HttpStatus.NOT_FOUND,
            "Resource not found"
        );
        when(mockRequest.getDescription(false)).thenReturn("uri=/missing");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn("corr-789");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleHttpClientErrorException(exception, mockRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
    }

    @Test
    void testHandleHttpServerErrorExceptionInternalServerError() {
        String errorMessage = "Internal server error occurred";
        HttpServerErrorException exception = new HttpServerErrorException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            errorMessage
        );
        when(mockRequest.getDescription(false)).thenReturn("uri=/process");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn("server-err-123");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleHttpServerErrorException(exception, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("server-err-123", response.getBody().correlationId());
    }

    @Test
    void testHandleHttpServerErrorExceptionServiceUnavailable() {
        HttpServerErrorException exception = new HttpServerErrorException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service temporarily unavailable"
        );
        when(mockRequest.getDescription(false)).thenReturn("uri=/service");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn("svc-unavail");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleHttpServerErrorException(exception, mockRequest);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(503, response.getBody().status());
        assertEquals("Service Unavailable", response.getBody().error());
    }

    @Test
    void testHandleHttpServerErrorExceptionBadGateway() {
        HttpServerErrorException exception = new HttpServerErrorException(
            HttpStatus.BAD_GATEWAY,
            "Bad gateway"
        );
        when(mockRequest.getDescription(false)).thenReturn("uri=/gateway");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn("gateway-err");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleHttpServerErrorException(exception, mockRequest);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals(502, response.getBody().status());
        assertEquals("Bad Gateway", response.getBody().error());
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new IllegalArgumentException("Unexpected error");
        when(mockRequest.getDescription(false)).thenReturn("uri=/operation");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn("generic-err-001");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleGenericException(exception, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("An unexpected error occurred. Please try again later.",
                     response.getBody().message());
        assertEquals("/operation", response.getBody().path());
        assertEquals("generic-err-001", response.getBody().correlationId());
    }

    @Test
    void testHandleGenericExceptionNullPointerException() {
        NullPointerException exception = new NullPointerException();
        when(mockRequest.getDescription(false)).thenReturn("uri=/process-data");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn(null);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleGenericException(exception, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("N/A", response.getBody().correlationId());
    }

    @Test
    void testHandleGenericExceptionRuntimeException() {
        RuntimeException exception = new RuntimeException("System error");
        when(mockRequest.getDescription(false)).thenReturn("uri=/api/v1/users");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn("runtime-err-999");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleGenericException(exception, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("An unexpected error occurred. Please try again later.",
                     response.getBody().message());
    }

    @Test
    void testHandleHttpClientErrorExceptionWithoutCorrelationId() {
        HttpClientErrorException exception = new HttpClientErrorException(
            HttpStatus.BAD_REQUEST,
            "Invalid input"
        );
        when(mockRequest.getDescription(false)).thenReturn("uri=/input");
        when(mockRequest.getHeader("CPPCLIENTCORRELATIONID")).thenReturn(null);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleHttpClientErrorException(exception, mockRequest);

        assertEquals("N/A", response.getBody().correlationId());
    }
}
