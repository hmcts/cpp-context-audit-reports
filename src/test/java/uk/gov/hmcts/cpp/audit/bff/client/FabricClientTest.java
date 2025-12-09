package uk.gov.hmcts.cpp.audit.bff.client;

import com.azure.core.credential.AccessToken;
import com.azure.identity.DefaultAzureCredential;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import uk.gov.hmcts.cpp.audit.bff.config.FabricConfiguration;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineRequest;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FabricClientTest {

    @Mock
    private FabricConfiguration fabricConfiguration;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DefaultAzureCredential defaultAzureCredential;

    private ObjectMapper objectMapper;
    private FabricClient fabricClient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private void stubValidConfig() {
        when(fabricConfiguration.getTenantId()).thenReturn("test-tenant-id");
        when(fabricConfiguration.getWorkspaceId()).thenReturn("workspace-123");
        when(fabricConfiguration.getPipelineId()).thenReturn("pipeline-456");
        when(fabricConfiguration.getFabricBaseUrl()).thenReturn("https://api.fabric.microsoft.com/v1");
    }

    private void stubPipelineName() {
        when(fabricConfiguration.getPipelineName()).thenReturn("test-pipeline-name");
    }

    private void createClientWithValidConfig() {
        stubValidConfig();
        fabricClient = new FabricClient(fabricConfiguration, restTemplate, objectMapper, defaultAzureCredential);
    }

    private void setupAccessTokenMock() {
        AccessToken mockAccessToken = mock(AccessToken.class);
        when(mockAccessToken.getToken()).thenReturn("test-token");

        when(defaultAzureCredential.getToken(any()))
            .thenReturn(Mono.just(mockAccessToken));
    }

    @Test
    void testConstructorMissingWorkspaceId() {
        when(fabricConfiguration.getTenantId()).thenReturn("test-tenant-id");
        when(fabricConfiguration.getWorkspaceId()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
            new FabricClient(fabricConfiguration, restTemplate, objectMapper, defaultAzureCredential)
        );
    }

    @Test
    void testRunPipelineSuccess() {
        createClientWithValidConfig();
        setupAccessTokenMock();

        String requestingUser = "user@example.com";
        String userId = "user-123";
        String fromDate = "2024-01-01";
        String toDate = "2024-12-31";
        String correlationId = "corr-123";

        FabricPipelineRequest pipelineRequest = FabricPipelineRequest.builder()
            .requestingUser(requestingUser)
            .userId(userId)
            .fromDateUtc(fromDate)
            .toDateUtc(toDate)
            .build();

        String responseBody = "{\"runId\": \"run-id-789\", \"status\": \"Queued\"}";

        ResponseEntity<String> fabricResponse = ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(responseBody);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(fabricResponse);

        ResponseEntity<FabricPipelineResponse> response = fabricClient.runPipeline(pipelineRequest, correlationId);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("run-id-789", response.getBody().getRunId());
        assertEquals("Queued", response.getBody().getStatus());

        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testRunPipelineVerifyUrl() {
        createClientWithValidConfig();
        setupAccessTokenMock();

        FabricPipelineRequest pipelineRequest = FabricPipelineRequest.builder()
            .requestingUser("user@example.com")
            .userId("user-123")
            .fromDateUtc("2024-01-01")
            .toDateUtc("2024-12-31")
            .build();

        String responseBody = "{\"runId\": \"run-id-789\", \"status\": \"Queued\"}";
        ResponseEntity<String> fabricResponse = ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(responseBody);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(fabricResponse);

        fabricClient.runPipeline(pipelineRequest, "corr-123");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).postForEntity(urlCaptor.capture(), any(HttpEntity.class), eq(String.class));

        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains("workspaces/workspace-123"));
        assertTrue(capturedUrl.contains("items/pipeline-456"));
        assertTrue(capturedUrl.contains("jobs/instances"));
        assertTrue(capturedUrl.contains("api-version=2023-11-01"));
    }

    @Test
    void testRunPipelineVerifyRequestHeaders() {
        createClientWithValidConfig();
        setupAccessTokenMock();

        FabricPipelineRequest pipelineRequest = FabricPipelineRequest.builder()
            .requestingUser("user@example.com")
            .userId("user-123")
            .fromDateUtc("2024-01-01")
            .toDateUtc("2024-12-31")
            .build();

        String responseBody = "{\"runId\": \"run-id-789\", \"status\": \"Queued\"}";
        ResponseEntity<String> fabricResponse = ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(responseBody);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(fabricResponse);

        fabricClient.runPipeline(pipelineRequest, "corr-123");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), entityCaptor.capture(), eq(String.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertTrue(headers.get("Authorization").get(0).startsWith("Bearer "));
        assertEquals("corr-123", headers.get("X-Correlation-ID").get(0));
        assertEquals("application/json", headers.get("Accept").get(0));
    }

    @Test
    void testRunPipelineVerifyRequestBody() {
        createClientWithValidConfig();
        setupAccessTokenMock();

        String requestingUser = "user@example.com";
        String userId = "user-123";
        String fromDate = "2024-01-01";
        String toDate = "2024-12-31";

        FabricPipelineRequest pipelineRequest = FabricPipelineRequest.builder()
            .requestingUser(requestingUser)
            .userId(userId)
            .fromDateUtc(fromDate)
            .toDateUtc(toDate)
            .build();

        String responseBody = "{\"runId\": \"run-id-789\", \"status\": \"Queued\"}";
        ResponseEntity<String> fabricResponse = ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(responseBody);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(fabricResponse);

        fabricClient.runPipeline(pipelineRequest, "corr-123");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), entityCaptor.capture(), eq(String.class));

        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) capturedEntity.getBody();

        assertNotNull(body);
        assertEquals("Pipeline", body.get("jobType"));

        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) body.get("parameters");
        assertEquals(requestingUser, parameters.get("requestinguser"));
        assertEquals(userId, parameters.get("userid"));
        assertEquals(fromDate, parameters.get("from_dateutc"));
        assertEquals(toDate, parameters.get("to_dateutc"));
    }

    @Test
    void testRunPipelineUnexpectedStatus() {
        createClientWithValidConfig();
        setupAccessTokenMock();

        FabricPipelineRequest pipelineRequest = FabricPipelineRequest.builder()
            .requestingUser("user@example.com")
            .userId("user-123")
            .fromDateUtc("2024-01-01")
            .toDateUtc("2024-12-31")
            .build();

        ResponseEntity<String> fabricResponse = ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Internal Server Error");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(fabricResponse);

        ResponseEntity<FabricPipelineResponse> response = fabricClient.runPipeline(pipelineRequest, "corr-123");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testRunPipelineRestClientException() {
        createClientWithValidConfig();
        setupAccessTokenMock();

        FabricPipelineRequest pipelineRequest = FabricPipelineRequest.builder()
            .requestingUser("user@example.com")
            .userId("user-123")
            .fromDateUtc("2024-01-01")
            .toDateUtc("2024-12-31")
            .build();

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenThrow(new RestClientException("Connection timeout"));

        assertThrows(RuntimeException.class, () ->
            fabricClient.runPipeline(pipelineRequest, "corr-123")
        );
    }

    @Test
    void testRunPipelineEmptyResponseBody() {
        createClientWithValidConfig();
        stubPipelineName();
        setupAccessTokenMock();

        FabricPipelineRequest pipelineRequest = FabricPipelineRequest.builder()
            .requestingUser("user@example.com")
            .userId("user-123")
            .fromDateUtc("2024-01-01")
            .toDateUtc("2024-12-31")
            .build();

        ResponseEntity<String> fabricResponse = ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body("");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(fabricResponse);

        ResponseEntity<FabricPipelineResponse> response = fabricClient.runPipeline(pipelineRequest, "corr-123");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Queued", response.getBody().getStatus());
        assertEquals("test-pipeline-name", response.getBody().getPipelineName());
    }

    @Test
    void testConstructorMissingPipelineId() {
        when(fabricConfiguration.getTenantId()).thenReturn("test-tenant-id");
        when(fabricConfiguration.getWorkspaceId()).thenReturn("workspace-123");
        when(fabricConfiguration.getPipelineId()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
            new FabricClient(fabricConfiguration, restTemplate, objectMapper, defaultAzureCredential)
        );
    }

    @Test
    void testConstructorBlankPipelineId() {
        when(fabricConfiguration.getTenantId()).thenReturn("test-tenant-id");
        when(fabricConfiguration.getWorkspaceId()).thenReturn("workspace-123");
        when(fabricConfiguration.getPipelineId()).thenReturn("   ");

        assertThrows(IllegalArgumentException.class, () ->
            new FabricClient(fabricConfiguration, restTemplate, objectMapper, defaultAzureCredential)
        );
    }

}
